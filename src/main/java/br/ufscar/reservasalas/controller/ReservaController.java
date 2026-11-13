package br.ufscar.reservasalas.controller;

import br.ufscar.reservasalas.config.UsuarioLogado;
import br.ufscar.reservasalas.service.RegraNegocioException;
import br.ufscar.reservasalas.service.ReservaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

/** Reservas do aluno (RF011 a RF020). */
@Controller
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping("/reservas")
    public String reservar(@RequestParam Long salaId,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora,
                           HttpSession sessao, RedirectAttributes flash) {
        UsuarioLogado usuario = (UsuarioLogado) sessao.getAttribute(UsuarioLogado.ATRIBUTO_SESSAO);
        try {
            reservaService.reservar(usuario.id(), salaId, data, hora);
            flash.addFlashAttribute("sucesso", "Reserva realizada com sucesso.");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/salas/" + salaId + "?data=" + data;
    }

    @GetMapping("/minhas-reservas")
    public String minhasReservas(HttpSession sessao, Model model) {
        UsuarioLogado usuario = (UsuarioLogado) sessao.getAttribute(UsuarioLogado.ATRIBUTO_SESSAO);
        model.addAttribute("reservas", reservaService.listarDoAluno(usuario.id()));
        return "reservas/minhas";
    }

    @PostMapping("/reservas/{id}/cancelar")
    public String cancelar(@PathVariable Long id, HttpSession sessao, RedirectAttributes flash) {
        UsuarioLogado usuario = (UsuarioLogado) sessao.getAttribute(UsuarioLogado.ATRIBUTO_SESSAO);
        try {
            reservaService.cancelarComoAluno(id, usuario.id());
            flash.addFlashAttribute("sucesso", "Reserva cancelada com sucesso.");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/minhas-reservas";
    }
}
