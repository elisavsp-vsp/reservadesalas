package br.ufscar.reservasalas.service;

import java.time.LocalTime;

/** Situacao de um bloco de horario de uma sala em uma data (RF010). */
public record HorarioView(LocalTime inicio, LocalTime fim, Status status) {

    public enum Status {
        DISPONIVEL,
        RESERVADO,
        MINHA_RESERVA,
        PASSADO
    }

    public boolean isDisponivel() {
        return status == Status.DISPONIVEL;
    }
}
