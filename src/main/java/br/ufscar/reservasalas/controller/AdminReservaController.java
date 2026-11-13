package br.ufscar.reservasalas.controller;

import br.ufscar.reservasalas.service.RegraNegocioException;
import br.ufscar.reservasalas.service.ReservaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Gerenciamento de reservas pelo administrador (RF025 a RF027). */
@Controller
public class AdminReservaController {

    private final ReservaService reservaService;

    public AdminReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping("/admin/reservas")
    public String listar(Model model) {
        model.addAttribute("reservas", reservaService.listarTodas());
        return "admin/reservas";
    }

    @PostMapping("/admin/reservas/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            reservaService.cancelarComoAdministrador(id);
            flash.addFlashAttribute("sucesso", "Reserva cancelada com sucesso.");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/reservas";
    }
}
