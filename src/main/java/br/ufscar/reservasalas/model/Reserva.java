package br.ufscar.reservasalas.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * RN013: Aluno -> Reserva -> Sala -> Data -> Horario.
 * A restricao unica garante, no banco, que nao existam duas reservas para a mesma
 * sala no mesmo periodo (RN012 / RF015), mesmo com acessos simultaneos.
 */
@Entity
@Table(name = "reservas", uniqueConstraints = @UniqueConstraint(
        name = "uk_reserva_sala_periodo", columnNames = {"sala_id", "data", "hora_inicio"}))
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    public Reserva() {
    }

    public Reserva(Usuario aluno, Sala sala, LocalDate data, LocalTime horaInicio, LocalTime horaFim) {
        this.aluno = aluno;
        this.sala = sala;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getAluno() { return aluno; }
    public void setAluno(Usuario aluno) { this.aluno = aluno; }
    public Sala getSala() { return sala; }
    public void setSala(Sala sala) { this.sala = sala; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
}
