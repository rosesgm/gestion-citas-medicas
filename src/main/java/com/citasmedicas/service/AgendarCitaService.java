/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.citasmedicas.service;

import com.citasmedicas.model.Cita;
import com.citasmedicas.model.Medico;
import com.citasmedicas.model.Paciente;
import com.citasmedicas.repository.CitaRepository;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author rosagabriela
 */
public class AgendarCitaService {
     public Cita agendarCita(Paciente paciente, Medico medico, LocalDate fecha, LocalTime hora, String motivo) {
         DisponibilidadService ds = new DisponibilidadService();

        if(!ds.estaDisponible(medico, fecha, hora)){
            throw new IllegalArgumentException("Horario no disponible");
}
        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setMotivo(motivo);
        cita.setEstado("CONFIRMADA");
        
        CitaRepository repository = new CitaRepository();
        repository.guardar(cita);
        
        return cita;
}
}
