package br.ufscar.reservasalas.config;

import br.ufscar.reservasalas.model.TipoUsuario;
import br.ufscar.reservasalas.model.Usuario;

import java.io.Serializable;

/** Dados do usuario autenticado guardados na sessao HTTP. */
public record UsuarioLogado(Long id, String nome, String ra, TipoUsuario tipo) implements Serializable {

    public static final String ATRIBUTO_SESSAO = "usuarioLogado";

    public static UsuarioLogado de(Usuario u) {
        return new UsuarioLogado(u.getId(), u.getNome(), u.getRa(), u.getTipo());
    }

    // getters estilo JavaBean para uso nos templates Thymeleaf
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getRa() { return ra; }

    public boolean isAdministrador() {
        return tipo == TipoUsuario.ADMINISTRADOR;
    }
}
