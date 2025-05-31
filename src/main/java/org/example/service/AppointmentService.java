package org.example.service;

import org.example.model.Appointment;
import org.example.model.User;
import org.example.repository.AppointmentRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired private UserRepository userRepository;

    public Appointment createAppointment(Long patientId, Long doctorId, LocalDateTime dateTime) {
        User patient = userRepository.findById(patientId).orElseThrow();
        User doctor = userRepository.findById(doctorId).orElseThrow();

        if (appointmentRepository.existsByDoctorAndDateTime(doctor, dateTime)) {
            throw new RuntimeException("Bu saatte doktorun başka randevusu var.");
        }

        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDoctor(doctor);
        appt.setDateTime(dateTime);
        appt.setStatus(Status.BEKLEMEDE);

        return appointmentRepository.save(appt);
    }

    public List<Appointment> getPastAppointments(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getAppointmentsForDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public Appointment updateStatus(Long appointmentId, Status status) {
        Appointment appt = appointmentRepository.findById(appointmentId).orElseThrow();
        appt.setStatus(status);
        return appointmentRepository.save(appt);
    }

    public Appointment addNote(Long appointmentId, String note) {
        Appointment appt = appointmentRepository.findById(appointmentId).orElseThrow();
        appt.setNote(note);
        return appointmentRepository.save(appt);
    }
}

