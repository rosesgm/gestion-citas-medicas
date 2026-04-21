/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.citasmedicas.service;

import com.citasmedicas.model.Cita;
import com.citasmedicas.model.Medico;
import com.citasmedicas.model.Paciente;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author rosagabriela
 */
public class AgendarCitaService {
     public Cita agendarCita(Paciente paciente, Medico medico, LocalDate fecha, LocalTime hora, String motivo) {
        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setMotivo(motivo);
        cita.setEstado("CONFIRMADA");

        return cita;
}
}
