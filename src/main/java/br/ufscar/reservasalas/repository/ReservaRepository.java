package br.ufscar.reservasalas.repository;

import br.ufscar.reservasalas.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    boolean existsBySalaIdAndDataAndHoraInicio(Long salaId, LocalDate data, LocalTime horaInicio);

    List<Reserva> findBySalaIdAndData(Long salaId, LocalDate data);

    @Query("select r from Reserva r join fetch r.sala where r.aluno.id = :alunoId "
            + "order by r.data desc, r.horaInicio desc")
    List<Reserva> findDoAluno(@Param("alunoId") Long alunoId);

    @Query("select r from Reserva r join fetch r.sala join fetch r.aluno "
            + "order by r.data desc, r.horaInicio desc")
    List<Reserva> findTodasComDetalhes();
}
