package br.ufscar.reservasalas.service;

/** Violacao de uma regra de negocio; a mensagem e exibida ao usuario (RNF002). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
