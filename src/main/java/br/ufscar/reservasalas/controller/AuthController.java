package br.ufscar.reservasalas.controller;

import br.ufscar.reservasalas.config.UsuarioLogado;
import br.ufscar.reservasalas.model.Usuario;
import br.ufscar.reservasalas.service.RegraNegocioException;
import br.ufscar.reservasalas.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/")
    public String raiz(HttpSession sessao) {
        UsuarioLogado u = (UsuarioLogado) sessao.getAttribute(UsuarioLogado.ATRIBUTO_SESSAO);
        if (u == null) {
            return "redirect:/login";
        }
        return u.isAdministrador() ? "redirect:/admin/reservas" : "redirect:/salas";
    }

    // ---------- Login (RF004, RF005) ----------

    @GetMapping("/login")
    public String loginForm(HttpSession sessao) {
        return sessao.getAttribute(UsuarioLogado.ATRIBUTO_SESSAO) != null ? "redirect:/" : "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String ra, @RequestParam String senha,
                        HttpSession sessao, Model model) {
        Optional<Usuario> usuario = usuarioService.autenticar(ra, senha);
        if (usuario.isEmpty()) {
            model.addAttribute("erro", "RA ou senha incorretos.");
            model.addAttribute("raInformado", ra);
            return "login";
        }
        sessao.setAttribute(UsuarioLogado.ATRIBUTO_SESSAO, UsuarioLogado.de(usuario.get()));
        return "redirect:/";
    }

    // ---------- Cadastro (RF001 a RF003) ----------

    @GetMapping("/cadastro")
    public String cadastroForm(Model model) {
        model.addAttribute("cadastroForm", new CadastroForm());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("cadastroForm") CadastroForm form,
                            BindingResult resultado, Model model, RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            return "cadastro";
        }
        try {
            usuarioService.cadastrarAluno(form.getNome(), form.getEmail(), form.getRa(), form.getSenha());
        } catch (RegraNegocioException e) {
            resultado.rejectValue("ra", "ra.duplicado", e.getMessage());
            return "cadastro";
        }
        flash.addFlashAttribute("sucesso", "Cadastro realizado com sucesso. Faça login para continuar.");
        return "redirect:/login";
    }

    // ---------- Logout (RF006) ----------

    @PostMapping("/logout")
    public String logout(HttpSession sessao, RedirectAttributes flash) {
        sessao.invalidate();
        return "redirect:/login?logout";
    }
}
