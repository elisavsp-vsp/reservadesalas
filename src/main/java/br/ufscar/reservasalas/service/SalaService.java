package br.ufscar.reservasalas.service;

import br.ufscar.reservasalas.model.Sala;
import br.ufscar.reservasalas.repository.SalaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalaService {

    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    @Transactional(readOnly = true)
    public List<Sala> listar() {
        return salaRepository.findAllByOrderByIdentificacaoAsc();
    }

    @Transactional(readOnly = true)
    public Sala buscar(Long id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Sala não encontrada."));
    }

    /** RF021 (cadastro) e RF022 (edicao). */
    @Transactional
    public Sala salvar(Sala sala) {
        sala.setIdentificacao(sala.getIdentificacao().trim());
        sala.setLocalizacao(sala.getLocalizacao().trim());

        boolean duplicada = sala.getId() == null
                ? salaRepository.existsByIdentificacaoIgnoreCase(sala.getIdentificacao())
                : salaRepository.existsByIdentificacaoIgnoreCaseAndIdNot(sala.getIdentificacao(), sala.getId());
        if (duplicada) {
            throw new RegraNegocioException("Já existe uma sala com esta identificação.");
        }

        if (sala.getId() == null) {
            return salaRepository.save(sala);
        }
        Sala existente = buscar(sala.getId());
        existente.setIdentificacao(sala.getIdentificacao());
        existente.setLocalizacao(sala.getLocalizacao());
        existente.setCapacidade(sala.getCapacidade());
        existente.setSituacao(sala.getSituacao());
        return salaRepository.save(existente);
    }

    /** RF023. As reservas da sala sao removidas em cascata. */
    @Transactional
    public void remover(Long id) {
        salaRepository.delete(buscar(id));
    }
}
