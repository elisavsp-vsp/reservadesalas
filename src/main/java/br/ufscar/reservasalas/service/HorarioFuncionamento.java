package br.ufscar.reservasalas.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras de funcionamento da biblioteca (RN001 a RN005, RN010).
 * Segunda a sexta, 08:00-12:00 e 14:00-20:00. Reservas em blocos de 1 hora.
 */
public final class HorarioFuncionamento {

    private static final int[][] PERIODOS = {{8, 12}, {14, 20}};

    private HorarioFuncionamento() {
    }

    public static boolean diaUtil(LocalDate data) {
        DayOfWeek d = data.getDayOfWeek();
        return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
    }

    /** Horarios de inicio dos blocos de 1 hora: 08..11 e 14..19. */
    public static List<LocalTime> horariosDeInicio() {
        List<LocalTime> lista = new ArrayList<>();
        for (int[] periodo : PERIODOS) {
            for (int h = periodo[0]; h < periodo[1]; h++) {
                lista.add(LocalTime.of(h, 0));
            }
        }
        return lista;
    }

    public static boolean horarioValido(LocalTime inicio) {
        return horariosDeInicio().contains(inicio);
    }

    public static LocalTime fimDoBloco(LocalTime inicio) {
        return inicio.plusHours(1);
    }
}
