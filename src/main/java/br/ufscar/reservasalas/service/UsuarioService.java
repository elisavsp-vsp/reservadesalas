package br.ufscar.reservasalas.service;

import br.ufscar.reservasalas.model.TipoUsuario;
import br.ufscar.reservasalas.model.Usuario;
import br.ufscar.reservasalas.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** RF001, RF003, RN006, RN007. */
    @Transactional
    public Usuario cadastrarAluno(String nome, String email, String ra, String senha) {
        String raLimpo = ra == null ? "" : ra.trim();
        if (usuarioRepository.existsByRa(raLimpo)) {
            throw new RegraNegocioException("Já existe uma conta cadastrada com este RA.");
        }
        Usuario aluno = new Usuario(nome.trim(), email.trim(), raLimpo,
                passwordEncoder.encode(senha), TipoUsuario.ALUNO);
        return usuarioRepository.save(aluno);
    }

    /** RF004 e RF005. Retorna vazio quando RA ou senha nao conferem. */
    @Transactional(readOnly = true)
    public Optional<Usuario> autenticar(String ra, String senha) {
        if (ra == null || senha == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByRa(ra.trim())
                .filter(u -> passwordEncoder.matches(senha, u.getSenhaHash()));
    }

    @Transactional
    public Usuario criarAdministrador(String nome, String email, String ra, String senha) {
        return usuarioRepository.save(new Usuario(nome, email, ra,
                passwordEncoder.encode(senha), TipoUsuario.ADMINISTRADOR));
    }
}
