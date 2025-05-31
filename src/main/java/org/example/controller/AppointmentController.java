package org.example.controller;

import org.example.enums.Status;
import org.example.model.Appointment;
import org.example.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // Hasta -> Randevu alma
    @PostMapping("/create")
    public ResponseEntity<Appointment> create(@RequestBody Map<String, String> data) {
        Long patientId = Long.parseLong(data.get("patientId"));
        Long doctorId = Long.parseLong(data.get("doctorId"));
        LocalDateTime dateTime = LocalDateTime.parse(data.get("dateTime"));

        return ResponseEntity.ok(appointmentService.createAppointment(patientId, doctorId, dateTime));
    }

    // Hasta -> geçmiş randevular
    @GetMapping("/past/{patientId}")
    public ResponseEntity<List<Appointment>> getPastAppointments(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getPastAppointments(patientId));
    }

    // Doktor -> kendi randevularını görme
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getAppointmentsForDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForDoctor(doctorId));
    }

    // Doktor -> randevu onaylama / reddetme
    @PutMapping("/{appointmentId}/status")
    public ResponseEntity<Appointment> updateStatus(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> body
    ) {
        Status status = Status.valueOf(body.get("status"));
        return ResponseEntity.ok(appointmentService.updateStatus(appointmentId, status));
    }

    // Doktor -> not ekleme
    @PutMapping("/{appointmentId}/note")
    public ResponseEntity<Appointment> addNote(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> body
    ) {
        String note = body.get("note");
        return ResponseEntity.ok(appointmentService.addNote(appointmentId, note));
    }
}
