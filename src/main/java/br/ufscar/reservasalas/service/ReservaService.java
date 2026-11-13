package br.ufscar.reservasalas.service;

import br.ufscar.reservasalas.model.Reserva;
import br.ufscar.reservasalas.model.Sala;
import br.ufscar.reservasalas.model.SituacaoSala;
import br.ufscar.reservasalas.model.Usuario;
import br.ufscar.reservasalas.repository.ReservaRepository;
import br.ufscar.reservasalas.repository.SalaRepository;
import br.ufscar.reservasalas.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final SalaRepository salaRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    public ReservaService(ReservaRepository reservaRepository, SalaRepository salaRepository,
                          UsuarioRepository usuarioRepository, Clock clock) {
        this.reservaRepository = reservaRepository;
        this.salaRepository = salaRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    /** RF009 e RF010: horarios da sala na data, indicando disponiveis e reservados. */
    @Transactional(readOnly = true)
    public List<HorarioView> horariosDoDia(Long salaId, LocalDate data, Long usuarioId) {
        if (!HorarioFuncionamento.diaUtil(data)) {
            return List.of();
        }
        List<Reserva> reservas = reservaRepository.findBySalaIdAndData(salaId, data);
        Set<LocalTime> ocupados = reservas.stream().map(Reserva::getHoraInicio).collect(Collectors.toSet());
        Set<LocalTime> meus = reservas.stream()
                .filter(r -> r.getAluno().getId().equals(usuarioId))
                .map(Reserva::getHoraInicio).collect(Collectors.toSet());

        LocalDateTime agora = LocalDateTime.now(clock);
        return HorarioFuncionamento.horariosDeInicio().stream().map(inicio -> {
            HorarioView.Status status;
            if (meus.contains(inicio)) {
                status = HorarioView.Status.MINHA_RESERVA;
            } else if (ocupados.contains(inicio)) {
                status = HorarioView.Status.RESERVADO;
            } else if (!data.atTime(inicio).isAfter(agora)) {
                status = HorarioView.Status.PASSADO;
            } else {
                status = HorarioView.Status.DISPONIVEL;
            }
            return new HorarioView(inicio, HorarioFuncionamento.fimDoBloco(inicio), status);
        }).toList();
    }

    /** RF011 a RF016, RN009 a RN013. */
    @Transactional
    public Reserva reservar(Long alunoId, Long salaId, LocalDate data, LocalTime horaInicio) {
        if (data == null || horaInicio == null) {
            throw new RegraNegocioException("Selecione a data e o horário da reserva.");
        }
        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
        if (aluno.isAdministrador()) {
            throw new RegraNegocioException("Somente alunos podem realizar reservas.");
        }
        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new RegraNegocioException("Sala não encontrada."));

        // RF016 / RN001 / RN005: dias de funcionamento
        if (!HorarioFuncionamento.diaUtil(data)) {
            throw new RegraNegocioException("A biblioteca não funciona aos sábados e domingos.");
        }
        // RF016 / RN002 / RN003 / RN004 / RN010: horarios de funcionamento
        if (!HorarioFuncionamento.horarioValido(horaInicio)) {
            throw new RegraNegocioException(
                    "Horário fora do funcionamento da biblioteca (08:00–12:00 e 14:00–20:00).");
        }
        if (!data.atTime(horaInicio).isAfter(LocalDateTime.now(clock))) {
            throw new RegraNegocioException("Não é possível reservar um horário que já passou.");
        }
        // RN011: sala precisa estar em condicoes de uso
        if (sala.getSituacao() != SituacaoSala.DISPONIVEL) {
            throw new RegraNegocioException("Esta sala está indisponível para reservas no momento.");
        }
        // RF012 / RF015 / RN012: conflito de reservas
        if (reservaRepository.existsBySalaIdAndDataAndHoraInicio(salaId, data, horaInicio)) {
            throw new RegraNegocioException("Horário indisponível.");
        }

        try {
            return reservaRepository.saveAndFlush(new Reserva(aluno, sala, data, horaInicio,
                    HorarioFuncionamento.fimDoBloco(horaInicio)));
        } catch (DataIntegrityViolationException e) {
            // outra requisicao reservou o mesmo horario ao mesmo tempo
            throw new RegraNegocioException("Horário indisponível.");
        }
    }

    /** RF017 */
    @Transactional(readOnly = true)
    public List<Reserva> listarDoAluno(Long alunoId) {
        return reservaRepository.findDoAluno(alunoId);
    }

    /** RF025 */
    @Transactional(readOnly = true)
    public List<Reserva> listarTodas() {
        return reservaRepository.findTodasComDetalhes();
    }

    /** RF018, RF019, RN014, RN015. */
    @Transactional
    public void cancelarComoAluno(Long reservaId, Long alunoId) {
        Reserva reserva = buscar(reservaId);
        if (!reserva.getAluno().getId().equals(alunoId)) {
            throw new RegraNegocioException("Você só pode cancelar as suas próprias reservas.");
        }
        reservaRepository.delete(reserva);
    }

    /** RF026 e RN016. */
    @Transactional
    public void cancelarComoAdministrador(Long reservaId) {
        reservaRepository.delete(buscar(reservaId));
    }

    private Reserva buscar(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));
    }
}
