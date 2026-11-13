package br.ufscar.reservasalas.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * RNF006: exige autenticacao para as funcionalidades de reserva.
 * RNF007: area /admin/** somente para administradores.
 */
@Component
public class AutorizacaoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession sessao = request.getSession(false);
        UsuarioLogado usuario = sessao == null ? null
                : (UsuarioLogado) sessao.getAttribute(UsuarioLogado.ATRIBUTO_SESSAO);

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String caminho = request.getRequestURI().substring(request.getContextPath().length());
        boolean areaAdmin = caminho.startsWith("/admin");
        if (areaAdmin && !usuario.isAdministrador()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso restrito a administradores.");
            return false;
        }
        if (!areaAdmin && usuario.isAdministrador()
                && (caminho.startsWith("/reservas") || caminho.startsWith("/minhas-reservas"))) {
            // administradores usam a area administrativa para gerenciar reservas
            response.sendRedirect(request.getContextPath() + "/admin/reservas");
            return false;
        }
        return true;
    }
}
