package br.ufscar.reservasalas.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** RF001 e RF002: campos obrigatorios do cadastro do aluno. */
public class CadastroForm {

    @NotBlank(message = "Informe o nome completo.")
    private String nome;

    @NotBlank(message = "Informe o e-mail.")
    @Email(message = "Informe um e-mail válido.")
    private String email;

    @NotBlank(message = "Informe o RA.")
    private String ra;

    @NotBlank(message = "Informe a senha.")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
    private String senha;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRa() { return ra; }
    public void setRa(String ra) { this.ra = ra; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
