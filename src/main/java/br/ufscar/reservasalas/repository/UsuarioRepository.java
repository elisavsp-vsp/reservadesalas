package br.ufscar.reservasalas.repository;

import br.ufscar.reservasalas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByRa(String ra);

    boolean existsByRa(String ra);
}
