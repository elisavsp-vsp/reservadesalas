package br.ufscar.reservasalas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "salas", uniqueConstraints = @UniqueConstraint(name = "uk_sala_identificacao", columnNames = "identificacao"))
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe a identificação da sala.")
    @Column(nullable = false, length = 60)
    private String identificacao;

    @NotBlank(message = "Informe a localização da sala.")
    @Column(nullable = false)
    private String localizacao;

    @NotNull(message = "Informe a capacidade da sala.")
    @Min(value = 1, message = "A capacidade deve ser de pelo menos 1 pessoa.")
    @Column(nullable = false)
    private Integer capacidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoSala situacao = SituacaoSala.DISPONIVEL;

    /** Ao remover uma sala (RF023), suas reservas sao removidas junto. */
    @OneToMany(mappedBy = "sala", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Reserva> reservas = new ArrayList<>();

    public Sala() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdentificacao() { return identificacao; }
    public void setIdentificacao(String identificacao) { this.identificacao = identificacao; }
    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }
    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }
    public SituacaoSala getSituacao() { return situacao; }
    public void setSituacao(SituacaoSala situacao) { this.situacao = situacao; }
    public List<Reserva> getReservas() { return reservas; }
    public void setReservas(List<Reserva> reservas) { this.reservas = reservas; }
}
