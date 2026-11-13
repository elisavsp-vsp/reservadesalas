package br.ufscar.reservasalas;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Testa as paginas Thymeleaf, os controladores e o controle de acesso ponta a ponta. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:teste-web;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class FluxoWebTest {

    @Autowired MockMvc mvc;

    private HttpSession login(String ra, String senha) throws Exception {
        MvcResult r = mvc.perform(post("/login").param("ra", ra).param("senha", senha))
                .andExpect(status().is3xxRedirection()).andReturn();
        return r.getRequest().getSession(false);
    }

    private LocalDate proximaTerca() {
        LocalDate d = LocalDate.now().plusDays(1);
        while (d.getDayOfWeek() != java.time.DayOfWeek.TUESDAY) {
            d = d.plusDays(1);
        }
        return d.plusWeeks(1);
    }

    @Test
    void semLoginRedirecionaParaLogin() throws Exception {
        mvc.perform(get("/salas")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/minhas-reservas")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/admin/salas")).andExpect(redirectedUrl("/login"));
    }

    @Test
    void loginComDadosErradosMostraMensagem() throws Exception {
        mvc.perform(post("/login").param("ra", "824695").param("senha", "x"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("RA ou senha incorretos.")));
    }

    @Test
    void cadastroValidaCamposERaDuplicado() throws Exception {
        mvc.perform(post("/cadastro").param("nome", "").param("email", "").param("ra", "").param("senha", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Informe o nome completo.")));

        mvc.perform(post("/cadastro").param("nome", "Fulano").param("email", "f@x.com")
                        .param("ra", "824695").param("senha", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Já existe uma conta")));

        mvc.perform(post("/cadastro").param("nome", "Fulano").param("email", "f@x.com")
                        .param("ra", "700001").param("senha", "123456"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void alunoNaoAcessaAreaAdministrativa() throws Exception {
        HttpSession aluno = login("824695", "123456");
        mvc.perform(get("/admin/salas").session((org.springframework.mock.web.MockHttpSession) aluno))
                .andExpect(status().isForbidden());
    }

    @Test
    void alunoReservaEVeNasMinhasReservasEAdminCancela() throws Exception {
        var aluno = (org.springframework.mock.web.MockHttpSession) login("824695", "123456");
        var admin = (org.springframework.mock.web.MockHttpSession) login("admin", "admin123");
        LocalDate data = proximaTerca();

        mvc.perform(get("/salas").session(aluno)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sala 01")));

        mvc.perform(get("/salas/1").param("data", data.toString()).session(aluno))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Reservar")));

        mvc.perform(post("/reservas").param("salaId", "1").param("data", data.toString()).param("hora", "16:00")
                        .session(aluno))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("sucesso", "Reserva realizada com sucesso."));

        // conflito
        mvc.perform(post("/reservas").param("salaId", "1").param("data", data.toString()).param("hora", "16:00")
                        .session(aluno))
                .andExpect(flash().attribute("erro", "Horário indisponível."));

        mvc.perform(get("/minhas-reservas").session(aluno)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("16:00")));

        MvcResult todas = mvc.perform(get("/admin/reservas").session(admin)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("824695"))).andReturn();
        assertThat(todas.getResponse().getContentAsString()).contains("Gabriel Augusto Crnkovic");

        mvc.perform(post("/logout").session(aluno)).andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void adminCadastraEditaERemoveSala() throws Exception {
        var admin = (org.springframework.mock.web.MockHttpSession) login("admin", "admin123");

        mvc.perform(post("/admin/salas").param("identificacao", "").param("localizacao", "")
                        .param("capacidade", "0").param("situacao", "DISPONIVEL").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Informe a identificação da sala.")));

        mvc.perform(post("/admin/salas").param("identificacao", "Sala Teste").param("localizacao", "Anexo")
                        .param("capacidade", "5").param("situacao", "DISPONIVEL").session(admin))
                .andExpect(redirectedUrl("/admin/salas"))
                .andExpect(flash().attribute("sucesso", "Sala cadastrada com sucesso."));

        mvc.perform(get("/admin/salas").session(admin))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sala Teste")));
    }
}
