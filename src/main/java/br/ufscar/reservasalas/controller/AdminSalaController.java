package br.ufscar.reservasalas.controller;

import br.ufscar.reservasalas.model.Sala;
import br.ufscar.reservasalas.model.SituacaoSala;
import br.ufscar.reservasalas.service.RegraNegocioException;
import br.ufscar.reservasalas.service.SalaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Gerenciamento de salas pelo administrador (RF021 a RF024). */
@Controller
public class AdminSalaController {

    private final SalaService salaService;

    public AdminSalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @GetMapping("/admin/salas")
    public String listar(Model model) {
        model.addAttribute("salas", salaService.listar());
        return "admin/salas";
    }

    @GetMapping("/admin/salas/nova")
    public String nova(Model model) {
        model.addAttribute("sala", new Sala());
        model.addAttribute("situacoes", SituacaoSala.values());
        return "admin/sala-form";
    }

    @GetMapping("/admin/salas/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        try {
            model.addAttribute("sala", salaService.buscar(id));
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
            return "redirect:/admin/salas";
        }
        model.addAttribute("situacoes", SituacaoSala.values());
        return "admin/sala-form";
    }

    @PostMapping("/admin/salas")
    public String salvar(@Valid @ModelAttribute("sala") Sala sala, BindingResult resultado,
                         Model model, RedirectAttributes flash) {
        model.addAttribute("situacoes", SituacaoSala.values());
        if (resultado.hasErrors()) {
            return "admin/sala-form";
        }
        boolean novo = sala.getId() == null;
        try {
            salaService.salvar(sala);
        } catch (RegraNegocioException e) {
            resultado.rejectValue("identificacao", "identificacao.duplicada", e.getMessage());
            return "admin/sala-form";
        }
        flash.addFlashAttribute("sucesso", novo ? "Sala cadastrada com sucesso." : "Sala atualizada com sucesso.");
        return "redirect:/admin/salas";
    }

    @PostMapping("/admin/salas/{id}/remover")
    public String remover(@PathVariable Long id, RedirectAttributes flash) {
        try {
            salaService.remover(id);
            flash.addFlashAttribute("sucesso", "Sala removida com sucesso.");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/salas";
    }
}
