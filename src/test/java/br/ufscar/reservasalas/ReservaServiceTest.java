package br.ufscar.reservasalas;

import br.ufscar.reservasalas.model.Reserva;
import br.ufscar.reservasalas.model.Sala;
import br.ufscar.reservasalas.model.Usuario;
import br.ufscar.reservasalas.repository.SalaRepository;
import br.ufscar.reservasalas.repository.UsuarioRepository;
import br.ufscar.reservasalas.service.HorarioView;
import br.ufscar.reservasalas.service.RegraNegocioException;
import br.ufscar.reservasalas.service.ReservaService;
import br.ufscar.reservasalas.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Relogio fixo: segunda-feira, 09/11/2026, 07:00. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:teste-servico;DB_CLOSE_DELAY=-1")
@Transactional
class ReservaServiceTest {

    static final LocalDate TERCA = LocalDate.of(2026, 11, 10);
    static final LocalDate SABADO = LocalDate.of(2026, 11, 14);
    static final LocalDate DOMINGO = LocalDate.of(2026, 11, 15);

    @TestConfiguration
    static class RelogioFixo {
        @Bean
        @Primary
        Clock clockDeTeste() {
            return Clock.fixed(LocalDateTime.of(2026, 11, 9, 7, 0).toInstant(ZoneOffset.ofHours(-3)),
                    ZoneOffset.ofHours(-3));
        }
    }

    @Autowired ReservaService reservaService;
    @Autowired UsuarioService usuarioService;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired SalaRepository salaRepository;

    Usuario aluno1;
    Usuario aluno2;
    Usuario admin;
    Sala sala;

    @BeforeEach
    void preparar() {
        aluno1 = usuarioRepository.findByRa("824695").orElseThrow();
        admin = usuarioRepository.findByRa("admin").orElseThrow();
        aluno2 = usuarioRepository.findByRa("999999")
                .orElseGet(() -> usuarioService.cadastrarAluno("Outro Aluno", "outro@ufscar.br", "999999", "abcdef"));
        sala = salaRepository.findAllByOrderByIdentificacaoAsc().get(0); // Sala 01 (disponivel)
    }

    @Test
    void reservaValidaEhRegistradaComTodosOsVinculos() {
        Reserva r = reservaService.reservar(aluno1.getId(), sala.getId(), TERCA, LocalTime.of(15, 0));

        assertThat(r.getId()).isNotNull();
        assertThat(r.getHoraFim()).isEqualTo(LocalTime.of(16, 0));
        assertThat(reservaService.listarDoAluno(aluno1.getId())).extracting(Reserva::getId).contains(r.getId());
    }

    @Test
    void naoPermiteDuasReservasParaMesmaSalaNoMesmoPeriodo() {
        reservaService.reservar(aluno1.getId(), sala.getId(), TERCA, LocalTime.of(15, 0));

        assertThatThrownBy(() -> reservaService.reservar(aluno2.getId(), sala.getId(), TERCA, LocalTime.of(15, 0)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Horário indisponível.");
    }

    @Test
    void mesmoHorarioEmOutraSalaOuOutroDiaEhPermitido() {
        Sala outra = salaRepository.findAllByOrderByIdentificacaoAsc().get(1);
        reservaService.reservar(aluno1.getId(), sala.getId(), TERCA, LocalTime.of(15, 0));

        reservaService.reservar(aluno2.getId(), outra.getId(), TERCA, LocalTime.of(15, 0));
        reservaService.reservar(aluno2.getId(), sala.getId(), TERCA.plusDays(1), LocalTime.of(15, 0));
    }

    @Test
    void bloqueiaFinsDeSemana() {
        assertThatThrownBy(() -> reservaService.reservar(aluno1.getId(), sala.getId(), SABADO, LocalTime.of(9, 0)))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> reservaService.reservar(aluno1.getId(), sala.getId(), DOMINGO, LocalTime.of(9, 0)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void bloqueiaHorariosForaDoFuncionamento() {
        for (LocalTime h : List.of(LocalTime.of(7, 0), LocalTime.of(12, 0), LocalTime.of(13, 0),
                LocalTime.of(20, 0), LocalTime.of(21, 0))) {
            assertThatThrownBy(() -> reservaService.reservar(aluno1.getId(), sala.getId(), TERCA, h))
                    .as("horario %s", h)
                    .isInstanceOf(RegraNegocioException.class);
        }
    }

    @Test
    void aceitaPrimeiroEUltimoBlocoDeCadaPeriodo() {
        reservaService.reservar(aluno1.getId(), sala.getId(), TERCA, LocalTime.of(8, 0));
        reservaService.reservar(aluno1.getId(), sala.getId(), TERCA, LocalTime.of(11, 0));
        reservaService.reservar(aluno1.getId(), sala.getId(), TERCA, LocalTime.of(14, 0));
        reservaService.reservar(aluno1.getId(), sala.getId(), TERCA, LocalTime.of(19, 0));
    }

    @Test
    void bloqueiaHorarioQueJaPassou() {
        // relogio fixo: segunda-feira 07:00
        assertThatThrownBy(() -> reservaService.reservar(aluno1.getId(), sala.getId(),
                LocalDate.of(2026, 11, 9), LocalTime.of(6, 0))).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> reservaService.reservar(aluno1.getId(), sala.getId(),
                LocalDate.of(2026, 11, 6), LocalTime.of(9, 0))).isInstanceOf(RegraNegocioException.class);
        reservaService.reservar(aluno1.getId(), sala.getId(), LocalDate.of(2026, 11, 9), LocalTime.of(8, 0));
    }

    @Test
    void salaEmManutencaoNaoPodeSerReservada() {
        Sala manutencao = salaRepository.findAllByOrderByIdentificacaoAsc().get(3); // Sala 04
        assertThatThrownBy(() -> reservaService.reservar(aluno1.getId(), manutencao.getId(), TERCA, LocalTime.of(9, 0)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("indisponível");
    }

    @Test
    void administradorNaoFazReservas() {
        assertThatThrownBy(() -> reservaService.reservar(admin.getId(), sala.getId(), TERCA, LocalTime.of(9, 0)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void alunoCancelaApenasAsPropriasReservas() {
        Reserva r = reservaService.reservar(aluno1.getId(), sala.getId(), TERCA, LocalTime.of(10, 0));

        assertThatThrownBy(() -> reservaService.cancelarComoAluno(r.getId(), aluno2.getId()))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(reservaService.listarDoAluno(aluno1.getId())).hasSize(1);

        reservaService.cancelarComoAluno(r.getId(), aluno1.getId());
        assertThat(reservaService.listarDoAluno(aluno1.getId())).isEmpty();
    }

    @Test
    void administradorCancelaQualquerReserva() {
        Reserva r = reservaService.reservar(aluno2.getId(), sala.getId(), TERCA, LocalTime.of(10, 0));

        reservaService.cancelarComoAdministrador(r.getId());

        assertThat(reservaService.listarTodas()).extracting(Reserva::getId).doesNotContain(r.getId());
    }

    @Test
    void horariosDoDiaIndicamDisponiveisReservadosEMinhas() {
        reservaService.reservar(aluno1.getId(), sala.getId(), TERCA, LocalTime.of(9, 0));
        reservaService.reservar(aluno2.getId(), sala.getId(), TERCA, LocalTime.of(10, 0));

        List<HorarioView> horarios = reservaService.horariosDoDia(sala.getId(), TERCA, aluno1.getId());

        assertThat(horarios).hasSize(10); // 08-12 (4) + 14-20 (6)
        assertThat(horarios).filteredOn(h -> h.inicio().equals(LocalTime.of(9, 0)))
                .extracting(HorarioView::status).containsExactly(HorarioView.Status.MINHA_RESERVA);
        assertThat(horarios).filteredOn(h -> h.inicio().equals(LocalTime.of(10, 0)))
                .extracting(HorarioView::status).containsExactly(HorarioView.Status.RESERVADO);
        assertThat(horarios).filteredOn(h -> h.inicio().equals(LocalTime.of(8, 0)))
                .extracting(HorarioView::status).containsExactly(HorarioView.Status.DISPONIVEL);
        assertThat(reservaService.horariosDoDia(sala.getId(), SABADO, aluno1.getId())).isEmpty();
    }

    @Test
    void naoPermiteRaDuplicadoESenhaFicaComHash() {
        assertThatThrownBy(() -> usuarioService.cadastrarAluno("Clone", "c@ufscar.br", "824695", "123456"))
                .isInstanceOf(RegraNegocioException.class);

        assertThat(aluno1.getSenhaHash()).isNotEqualTo("123456").startsWith("$2");
        assertThat(usuarioService.autenticar("824695", "123456")).isPresent();
        assertThat(usuarioService.autenticar("824695", "errada")).isEmpty();
        assertThat(usuarioService.autenticar("000000", "123456")).isEmpty();
    }
}
