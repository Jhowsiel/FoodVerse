package com.senac.food.verse;

import java.time.LocalDateTime;

public class Reserva {
    private int idReserva;
    private int idCliente;
    private String nomeCliente; // Campo auxiliar para exibição
    private LocalDateTime dataReserva;
    private int numPessoas;
    private String mesa;
    private String status; // "LIVRE", "OCUPADA", "RESERVADA"

    public Reserva() {}

    public Reserva(int idReserva, int idCliente, String nomeCliente, LocalDateTime dataReserva, int numPessoas, String mesa, String status) {
        this.idReserva = idReserva;
        this.idCliente = idCliente;
        this.nomeCliente = nomeCliente;
        this.dataReserva = dataReserva;
        this.numPessoas = numPessoas;
        this.mesa = mesa;
        this.status = status;
    }

    // Getters e Setters
    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int idReserva) { this.idReserva = idReserva; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
    public LocalDateTime getDataReserva() { return dataReserva; }
    public void setDataReserva(LocalDateTime dataReserva) { this.dataReserva = dataReserva; }
    public int getNumPessoas() { return numPessoas; }
    public void setNumPessoas(int numPessoas) { this.numPessoas = numPessoas; }
    public String getMesa() { return mesa; }
    public void setMesa(String mesa) { this.mesa = mesa; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}