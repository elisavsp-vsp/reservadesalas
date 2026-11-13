package br.ufscar.reservasalas.controller;

import br.ufscar.reservasalas.config.UsuarioLogado;
import br.ufscar.reservasalas.model.Sala;
import br.ufscar.reservasalas.service.HorarioFuncionamento;
import br.ufscar.reservasalas.service.RegraNegocioException;
import br.ufscar.reservasalas.service.ReservaService;
import br.ufscar.reservasalas.service.SalaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Clock;
import java.time.LocalDate;

/** Consulta de salas e de disponibilidade (RF007 a RF010). */
@Controller
public class SalaController {

    private final SalaService salaService;
    private final ReservaService reservaService;
    private final Clock clock;

    public SalaController(SalaService salaService, ReservaService reservaService, Clock clock) {
        this.salaService = salaService;
        this.reservaService = reservaService;
        this.clock = clock;
    }

    @GetMapping("/salas")
    public String listar(Model model) {
        model.addAttribute("salas", salaService.listar());
        return "salas/lista";
    }

    @GetMapping("/salas/{id}")
    public String detalhe(@PathVariable Long id,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                          HttpSession sessao, Model model, RedirectAttributes flash) {
        Sala sala;
        try {
            sala = salaService.buscar(id);
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
            return "redirect:/salas";
        }
        LocalDate dataConsulta = data != null ? data : LocalDate.now(clock);
        UsuarioLogado usuario = (UsuarioLogado) sessao.getAttribute(UsuarioLogado.ATRIBUTO_SESSAO);

        model.addAttribute("sala", sala);
        model.addAttribute("data", dataConsulta);
        model.addAttribute("hoje", LocalDate.now(clock));
        model.addAttribute("diaUtil", HorarioFuncionamento.diaUtil(dataConsulta));
        model.addAttribute("horarios", reservaService.horariosDoDia(id, dataConsulta, usuario.id()));
        return "salas/detalhe";
    }
}
