package br.ufscar.reservasalas.model;

public enum SituacaoSala {
    DISPONIVEL("Disponível"),
    MANUTENCAO("Em manutenção");

    private final String descricao;

    SituacaoSala(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
