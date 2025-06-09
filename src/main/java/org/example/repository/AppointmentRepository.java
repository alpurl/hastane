package org.example.repository; // Paket adınız 'org.example.repository' olduğunu varsayıyorum

import org.example.model.Appointment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import org.example.enums.Status; // Doğru enum'ı import ediyoruz

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Randevu çakışması kontrolü için metodun imzası düzeltildi
    // Servis katmanında Long doctorId kullanıldığı için direkt ID üzerinden sorgulama yapıyoruz.
    boolean existsByDoctorIdAndAppointmentDateTime(Long doctorId, LocalDateTime dateTime);
    // Hastanın geçmiş randevuları için sıralı metod eklendi
    List<Appointment> findByPatientIdOrderByAppointmentDateTimeDesc(Long patientId);

    // Doktorun tüm randevuları için sıralı metod eklendi
    List<Appointment> findByDoctorIdOrderByAppointmentDateTimeAsc(Long doctorId);

    // Doktorun belirli durumdaki randevuları (örn: BEKLEMEDE)
    // Enum tipi org.example.model.AppointmentStatus olarak düzeltildi
    List<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateTimeAsc(Long doctorId, Status status);

    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
}
