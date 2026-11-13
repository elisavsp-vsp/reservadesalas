# Sistema Web para Reserva de Salas de Estudo

Protótipo funcional baseado no *Documento de Requisitos* (Tópicos em Engenharia de Software – UFSCar).

**Stack:** Java 21 · Spring Boot 3.3 · Spring MVC · Thymeleaf · Spring Data JPA (Hibernate) · H2 · Bootstrap 5 (CDN)

## Como executar

Requisitos: JDK 21 e Maven 3.9+.

```bash
mvn spring-boot:run          # sobe em http://localhost:8080
mvn test                     # roda os testes automatizados
```

O banco H2 é criado em `./data/` (persiste entre execuções). Console H2: `http://localhost:8080/h2-console`
(JDBC URL `jdbc:h2:file:./data/reservasalas`, usuário `sa`, senha vazia).

### Usuários de demonstração (criados na primeira execução)

| Perfil        | RA       | Senha      |
|---------------|----------|------------|
| Aluno         | `824695` | `123456`   |
| Administrador | `admin`  | `admin123` |

Também são criadas 4 salas de exemplo (a "Sala 04" está em manutenção).

## Arquitetura MVC

```
br.ufscar.reservasalas
├── model/        Domínio + JPA: Usuario, Sala, Reserva, enums TipoUsuario e SituacaoSala
├── repository/   Persistência (Spring Data JPA)
├── service/      Regras de negócio: UsuarioService, SalaService, ReservaService, HorarioFuncionamento
├── controller/   Controladores MVC: Auth, Sala, Reserva, AdminSala, AdminReserva
└── config/       Interceptor de autenticação/autorização, BCrypt, carga inicial de dados
resources/templates/   Páginas Thymeleaf (view)
```

## Rastreabilidade dos requisitos

| Requisitos | Onde está |
|---|---|
| RF001–RF003, RN006–RN007 (cadastro, campos obrigatórios, RA único) | `AuthController`, `CadastroForm`, `UsuarioService`, restrição única em `Usuario.ra` |
| RF004–RF006, RN008 (login por RA e senha, validação, logout) | `AuthController`, `UsuarioService.autenticar` |
| RF007–RF010 (listar salas, informações, disponibilidade por data, horários livres/reservados) | `SalaController`, `ReservaService.horariosDoDia`, `salas/*.html` |
| RF011–RF016, RN009–RN013 (reservar, conflito, horários válidos) | `ReservaController`, `ReservaService.reservar`, `HorarioFuncionamento`, restrição única `(sala, data, hora_inicio)` em `Reserva` |
| RF017–RF020, RN014–RN015 (minhas reservas, cancelar só as próprias) | `ReservaController`, `ReservaService.cancelarComoAluno` |
| RF021–RF024 (CRUD de salas pelo admin) | `AdminSalaController`, `SalaService` |
| RF025–RF027, RN016 (admin vê e cancela qualquer reserva) | `AdminReservaController`, `ReservaService.cancelarComoAdministrador` |
| RN001–RN005, RN010 (seg–sex, 08–12 e 14–20) | `HorarioFuncionamento` |
| RNF001–RNF004 (interface simples, mensagens, responsiva, navegadores) | Bootstrap 5, `fragments/layout.html`, mensagens flash |
| RNF006–RNF007 (autenticação e controle de acesso) | `AutorizacaoInterceptor` (`/admin/**` só para administradores) |
| RNF008 (hash de senha) | `BCryptPasswordEncoder` |

## Decisões de projeto (pontos não especificados no documento)

- **Duração da reserva:** blocos fixos de 1 hora (08–09, …, 11–12, 14–15, …, 19–20), conforme o exemplo de RN012.
- **Horários que já passaram** não podem ser reservados.
- **Situação da sala (RF008):** `Disponível` ou `Em manutenção`; salas em manutenção não aceitam reservas.
- **Remover sala (RF023):** as reservas da sala são removidas junto (a tela pede confirmação).
- **Administradores** não fazem reservas; só gerenciam salas e reservas.
- **Senha do cadastro:** mínimo de 6 caracteres.
- **Concorrência:** além da verificação no serviço, o banco tem restrição única para impedir duas reservas simultâneas no mesmo período.
- O Bootstrap é carregado por CDN, então o navegador precisa de acesso à internet para o visual completo.
