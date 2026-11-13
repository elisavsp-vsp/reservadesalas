package br.ufscar.reservasalas.repository;

import br.ufscar.reservasalas.model.Sala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalaRepository extends JpaRepository<Sala, Long> {

    List<Sala> findAllByOrderByIdentificacaoAsc();

    boolean existsByIdentificacaoIgnoreCase(String identificacao);

    boolean existsByIdentificacaoIgnoreCaseAndIdNot(String identificacao, Long id);
}
