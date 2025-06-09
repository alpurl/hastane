package org.example.controller;

import org.example.model.Appointment;
import org.example.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.validation.Valid;
import org.example.dto.request.AppointmentCreateRequest;
import org.example.dto.request.AppointmentStatusUpdateRequest;
import org.example.dto.request.DoctorNoteRequest;
import org.example.exception.BadRequestException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.example.security.UserPrincipal;



@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    // Constructor Injection ile bağımlılıkları enjekte ediyoruz
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Hasta rolündeki bir kullanıcının yeni randevu oluşturmasını sağlar.
     *
     * @param currentUser Giriş yapmış hastanın bilgileri (Spring Security'den gelir)
     * @param request     Randevu oluşturma isteği DTO'su
     * @return Oluşturulan randevu bilgisiyle HTTP 201 Created yanıtı
     */
    @PreAuthorize("hasRole('PATIENT')") // Sadece hasta rolündekiler erişebilir
    @PostMapping // /api/appointments
    public ResponseEntity<Appointment> createAppointment(
            @AuthenticationPrincipal UserPrincipal currentUser, // Giriş yapan kullanıcının ID'si ve rolü
            @Valid @RequestBody AppointmentCreateRequest request // Validasyon için @Valid
    ) {
        // currentUser.getId() metodu, UserPrincipal implementasyonunuza göre hastanın ID'sini döndürmelidir.
        // Bu ID'nin gerçekten PATIENT rolünde olduğundan emin olma işlemi zaten AppointmentService içinde yapılır.
        Appointment newAppointment = appointmentService.createAppointment(
                currentUser.getId(),
                request.getDoctorId(),
                request.getAppointmentDateTime()
        );
        return new ResponseEntity<>(newAppointment, HttpStatus.CREATED); // Başarılı oluşturma için 201 Created
    }

    /**
     * Hasta rolündeki bir kullanıcının geçmiş randevularını görüntüler.
     * @param currentUser Giriş yapmış hastanın bilgileri
     * @return Hastanın geçmiş randevularının listesi
     */
    @PreAuthorize("hasRole('PATIENT')") // Sadece hasta rolündekiler erişebilir
    @GetMapping("/patient/past") // /api/appointments/patient/past
    public ResponseEntity<List<Appointment>> getPatientPastAppointments(
            @AuthenticationPrincipal UserPrincipal currentUser // PathVariable yerine kendi ID'si kullanılır
    ) {
        // currentUser.getId() metodu, UserPrincipal implementasyonunuza göre hastanın ID'sini döndürmelidir.
        List<Appointment> appointments = appointmentService.getPatientPastAppointments(currentUser.getId());
        return ResponseEntity.ok(appointments);
    }

    /**
     * Doktor rolündeki bir kullanıcının tüm randevularını görüntüler.
     *
     * @param currentUser Giriş yapmış doktorun bilgileri
     * @return Doktorun tüm randevularının listesi
     */
    @PreAuthorize("hasRole('DOKTOR')") // Sadece doktor rolündekiler erişebilir
    @GetMapping("/doctor/all") // /api/appointments/doctor/all
    public ResponseEntity<List<Appointment>> getDoctorAllAppointments(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        // currentUser.getId() metodu, UserPrincipal implementasyonunuza göre doktorun ID'sini döndürmelidir.
        List<Appointment> appointments = appointmentService.getDoctorAppointments(currentUser.getId());
        return ResponseEntity.ok(appointments);
    }

    /**
     * Doktor rolündeki bir kullanıcının beklemede olan randevu taleplerini görüntüler.
     *
     * @param currentUser Giriş yapmış doktorun bilgileri
     * @return Doktorun beklemede olan randevu taleplerinin listesi
     */
    @PreAuthorize("hasRole('DOKTOR')") // Sadece doktor rolündekiler erişebilir
    @GetMapping("/doctor/pending") // /api/appointments/doctor/pending
    public ResponseEntity<List<Appointment>> getDoctorPendingAppointments(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<Appointment> appointments = appointmentService.getDoctorPendingAppointments(currentUser.getId());
        return ResponseEntity.ok(appointments);
    }


    /**
     * Doktor rolündeki bir kullanıcının randevu durumunu güncellemesini sağlar (Onaylama/Reddetme/Tamamlama).
     *
     * @param appointmentId Güncellenecek randevunun ID'si
     * @param currentUser   Giriş yapmış doktorun bilgileri
     * @param request       Yeni durumu içeren DTO
     * @return Güncellenen randevu bilgisiyle HTTP 200 OK yanıtı
     */
    @PreAuthorize("hasRole('DOKTOR')") // Sadece doktor rolündekiler erişebilir
    @PutMapping("/{appointmentId}/status") // /api/appointments/{appointmentId}/status
    public ResponseEntity<Appointment> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody AppointmentStatusUpdateRequest request
    ) {
        // currentUser.getId() metodu, UserPrincipal implementasyonunuza göre doktorun ID'sini döndürmelidir.
        // Bu ID'nin ilgili randevunun doktoru olup olmadığı kontrolü Service katmanında yapılır.
        Appointment updatedAppointment = appointmentService.updateStatus(
                appointmentId,
                currentUser.getId(), // Güncellemeyi yapan doktorun ID'si
                request.getNewStatus()
        );
        return ResponseEntity.ok(updatedAppointment);
    }

    /**
     * Doktor rolündeki bir kullanıcının randevuya not eklemesini sağlar.
     *
     * @param appointmentId Not eklenecek randevunun ID'si
     * @param currentUser   Giriş yapmış doktorun bilgileri
     * @param request       Notu içeren DTO
     * @return Güncellenen randevu bilgisiyle HTTP 200 OK yanıtı
     */
    @PreAuthorize("hasRole('DOKTOR')") // Sadece doktor rolündekiler erişebilir
    @PutMapping("/{appointmentId}/note") // /api/appointments/{appointmentId}/note
    public ResponseEntity<Appointment> addDoctorNote(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody DoctorNoteRequest request
    ) {
        // currentUser.getId() metodu, UserPrincipal implementasyonunuza göre doktorun ID'sini döndürmelidir.
        // Bu ID'nin ilgili randevunun doktoru olup olmadığı kontrolü Service katmanında yapılır.
        Appointment updatedAppointment = appointmentService.addDoctorNote(
                appointmentId,
                currentUser.getId(), // Notu ekleyen doktorun ID'si
                request.getNotes()
        );
        return ResponseEntity.ok(updatedAppointment);
    }

    /**
     * Hem hasta hem de doktorun kendi randevularını ID ile detaylı görmesini sağlar.
     * Güvenlik kontrolü (sadece randevuyla ilgili olan kişi görebilir) Service katmanında veya burada yapılmalı.
     *
     * @param appointmentId Randevunun ID'si
     * @param currentUser   Giriş yapan kullanıcının bilgileri
     * @return Randevu bilgisiyle HTTP 200 OK yanıtı
     */
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')") // Hem hasta hem doktor görebilir
    @GetMapping("/{appointmentId}") // /api/appointments/{appointmentId}
    public ResponseEntity<Appointment> getAppointmentDetails(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);

        // Görüntüleme yetkilendirmesi: Sadece randevunun hastası veya doktoru görebilir
        if (!appointment.getPatient().getId().equals(currentUser.getId()) &&
            !appointment.getDoctor().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bu randevuyu görüntüleme yetkiniz yok.");
        }

        return ResponseEntity.ok(appointment);
    }
}
