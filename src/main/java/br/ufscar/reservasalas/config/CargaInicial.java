package br.ufscar.reservasalas.config;

import br.ufscar.reservasalas.model.Sala;
import br.ufscar.reservasalas.model.SituacaoSala;
import br.ufscar.reservasalas.repository.SalaRepository;
import br.ufscar.reservasalas.repository.UsuarioRepository;
import br.ufscar.reservasalas.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Dados de demonstracao para o prototipo (somente se o banco estiver vazio). */
@Component
public class CargaInicial implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final SalaRepository salaRepository;
    private final UsuarioService usuarioService;

    public CargaInicial(UsuarioRepository usuarioRepository, SalaRepository salaRepository,
                        UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.salaRepository = salaRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByRa("admin")) {
            usuarioService.criarAdministrador("Administrador da Biblioteca", "admin@biblioteca.ufscar.br",
                    "admin", "admin123");
        }
        if (!usuarioRepository.existsByRa("824695")) {
            usuarioService.cadastrarAluno("Gabriel Augusto Crnkovic", "gabriel@estudante.ufscar.br",
                    "824695", "123456");
        }
        if (salaRepository.count() == 0) {
            salaRepository.save(sala("Sala 01", "Térreo – Bloco A", 4, SituacaoSala.DISPONIVEL));
            salaRepository.save(sala("Sala 02", "Térreo – Bloco A", 6, SituacaoSala.DISPONIVEL));
            salaRepository.save(sala("Sala 03", "1º andar – Bloco B", 8, SituacaoSala.DISPONIVEL));
            salaRepository.save(sala("Sala 04", "1º andar – Bloco B", 10, SituacaoSala.MANUTENCAO));
        }
    }

    private Sala sala(String identificacao, String localizacao, int capacidade, SituacaoSala situacao) {
        Sala s = new Sala();
        s.setIdentificacao(identificacao);
        s.setLocalizacao(localizacao);
        s.setCapacidade(capacidade);
        s.setSituacao(situacao);
        return s;
    }
}
