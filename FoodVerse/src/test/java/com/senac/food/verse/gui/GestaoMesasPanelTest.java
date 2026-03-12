package com.senac.food.verse.gui;

import com.senac.food.verse.Reserva;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GestaoMesasPanelTest {

    @Test
    void classificaReservaNoHorarioDeCheckIn() {
        Reserva reserva = new Reserva();
        reserva.setDataReserva(LocalDateTime.of(2026, 3, 11, 19, 0));

        assertEquals("CHECK_IN",
                GestaoMesasPanel.classifyReservationStatus(reserva, LocalDateTime.of(2026, 3, 11, 19, 10)));
    }

    @Test
    void classificaReservaAtrasadaQuandoPassaDaJanela() {
        Reserva reserva = new Reserva();
        reserva.setDataReserva(LocalDateTime.of(2026, 3, 11, 18, 0));

        assertEquals("ATRASADA",
                GestaoMesasPanel.classifyReservationStatus(reserva, LocalDateTime.of(2026, 3, 11, 18, 30)));
    }

    @Test
    void classificaReservaFuturaComoProxima() {
        Reserva reserva = new Reserva();
        reserva.setDataReserva(LocalDateTime.of(2026, 3, 11, 21, 0));

        assertEquals("PROXIMA",
                GestaoMesasPanel.classifyReservationStatus(reserva, LocalDateTime.of(2026, 3, 11, 19, 0)));
    }
}
