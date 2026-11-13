package br.ufscar.reservasalas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_ra", columnNames = "ra"))
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String email;

    /** RN007: cada RA so pode estar associado a uma conta. */
    @Column(nullable = false, length = 30)
    private String ra;

    /** RNF008: somente o hash da senha e armazenado. */
    @Column(nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoUsuario tipo = TipoUsuario.ALUNO;

    public Usuario() {
    }

    public Usuario(String nome, String email, String ra, String senhaHash, TipoUsuario tipo) {
        this.nome = nome;
        this.email = email;
        this.ra = ra;
        this.senhaHash = senhaHash;
        this.tipo = tipo;
    }

    public boolean isAdministrador() {
        return tipo == TipoUsuario.ADMINISTRADOR;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRa() { return ra; }
    public void setRa(String ra) { this.ra = ra; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public TipoUsuario getTipo() { return tipo; }
    public void setTipo(TipoUsuario tipo) { this.tipo = tipo; }
}
