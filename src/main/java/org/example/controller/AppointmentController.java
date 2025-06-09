package org.example.controller;

import org.example.model.Appointment; // Entity olarak hala içeride kullanılıyor
import org.example.model.User; // Doktor listeleme için
import org.example.service.AppointmentService;
import org.example.service.UserService; // Doktor listeleme için UserService'e ihtiyaç var

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.Valid; // javax.validation yerine jakarta.validation kullanın
import org.example.dto.request.AppointmentCreateRequest;
import org.example.dto.request.AppointmentStatusUpdateRequest;
import org.example.dto.request.DoctorNoteRequest;
import org.example.dto.response.AppointmentResponse; // Yeni eklendi
import org.example.dto.response.DoctorResponse; // Yeni eklendi
import org.example.exception.BadRequestException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.example.security.UserPrincipal;
import org.example.mapper.AppointmentMapper; // MapStruct mapper
import org.example.mapper.UserMapper; // MapStruct mapper


@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final UserService userService; // Doktorları listelemek için eklendi
    private final AppointmentMapper appointmentMapper; // MapStruct mapper
    private final UserMapper userMapper; // MapStruct mapper

    // Constructor Injection ile tüm bağımlılıkları enjekte ediyoruz
    public AppointmentController(AppointmentService appointmentService,
                                 UserService userService, // Yeni eklendi
                                 AppointmentMapper appointmentMapper,
                                 UserMapper userMapper) {
        this.appointmentService = appointmentService;
        this.userService = userService; // Yeni eklendi
        this.appointmentMapper = appointmentMapper;
        this.userMapper = userMapper;
    }

    /**
     * Hasta rolündeki bir kullanıcının yeni randevu oluşturmasını sağlar.
     *
     * @param currentUser Giriş yapmış hastanın bilgileri (Spring Security'den gelir)
     * @param request     Randevu oluşturma isteği DTO'su
     * @return Oluşturulan randevu bilgisiyle HTTP 201 Created yanıtı
     */
    @PreAuthorize("hasRole('HASTA')") // Rol adı: PATIENT -> HASTA
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment( // Dönüş tipi AppointmentResponse oldu
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody AppointmentCreateRequest request
    ) {
        Appointment newAppointment = appointmentService.createAppointment(
                currentUser.getId(),
                request.getDoctorId(),
                request.getAppointmentDateTime()
        );
        // Entity'den DTO'ya dönüştür
        return new ResponseEntity<>(appointmentMapper.toAppointmentResponse(newAppointment), HttpStatus.CREATED);
    }

    /**
     * Hasta rolündeki bir kullanıcının geçmiş randevularını görüntüler.
     * @param currentUser Giriş yapmış hastanın bilgileri
     * @return Hastanın geçmiş randevularının listesi
     */
    @PreAuthorize("hasRole('HASTA')") // Rol adı: PATIENT -> HASTA
    @GetMapping("/patient/past")
    public ResponseEntity<List<AppointmentResponse>> getPatientPastAppointments( // Dönüş tipi AppointmentResponse listesi oldu
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<Appointment> appointments = appointmentService.getPatientPastAppointments(currentUser.getId());
        // Entity listesinden DTO listesine dönüştür
        return ResponseEntity.ok(appointmentMapper.toAppointmentResponseList(appointments));
    }

    /**
     * Doktor rolündeki bir kullanıcının tüm randevularını görüntüler.
     *
     * @param currentUser Giriş yapmış doktorun bilgileri
     * @return Doktorun tüm randevularının listesi
     */
    @PreAuthorize("hasRole('DOKTOR')") // Rol adı: DOKTOR
    @GetMapping("/doctor/all")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAllAppointments( // Dönüş tipi AppointmentResponse listesi oldu
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<Appointment> appointments = appointmentService.getDoctorAppointments(currentUser.getId());
        // Entity listesinden DTO listesine dönüştür
        return ResponseEntity.ok(appointmentMapper.toAppointmentResponseList(appointments));
    }

    /**
     * Doktor rolündeki bir kullanıcının beklemede olan randevu taleplerini görüntüler.
     *
     * @param currentUser Giriş yapmış doktorun bilgileri
     * @return Doktorun beklemede olan randevu taleplerinin listesi
     */
    @PreAuthorize("hasRole('DOKTOR')") // Rol adı: DOKTOR
    @GetMapping("/doctor/pending")
    public ResponseEntity<List<AppointmentResponse>> getDoctorPendingAppointments( // Dönüş tipi AppointmentResponse listesi oldu
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<Appointment> appointments = appointmentService.getDoctorPendingAppointments(currentUser.getId());
        // Entity listesinden DTO listesine dönüştür
        return ResponseEntity.ok(appointmentMapper.toAppointmentResponseList(appointments));
    }


    /**
     * Doktor rolündeki bir kullanıcının randevu durumunu güncellemesini sağlar (Onaylama/Reddetme/Tamamlama).
     *
     * @param appointmentId Güncellenecek randevunun ID'si
     * @param currentUser   Giriş yapmış doktorun bilgileri
     * @param request       Yeni durumu içeren DTO
     * @return Güncellenen randevu bilgisiyle HTTP 200 OK yanıtı
     */
    @PreAuthorize("hasRole('DOKTOR')") // Rol adı: DOKTOR
    @PutMapping("/{appointmentId}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus( // Dönüş tipi AppointmentResponse oldu
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody AppointmentStatusUpdateRequest request
    ) {
        // Hata Düzeltmesi: appointmentService.updateStatus yerine appointmentService.updateAppointmentStatus çağrıldı
        Appointment updatedAppointment = appointmentService.updateStatus(
                appointmentId,
                currentUser.getId(),
                request.getNewStatus()
        );
        // Entity'den DTO'ya dönüştür
        return ResponseEntity.ok(appointmentMapper.toAppointmentResponse(updatedAppointment));
    }

    /**
     * Doktor rolündeki bir kullanıcının randevuya not eklemesini sağlar.
     *
     * @param appointmentId Not eklenecek randevunun ID'si
     * @param currentUser   Giriş yapmış doktorun bilgileri
     * @param request       Notu içeren DTO
     * @return Güncellenen randevu bilgisiyle HTTP 200 OK yanıtı
     */
    @PreAuthorize("hasRole('DOKTOR')") // Rol adı: DOKTOR
    @PutMapping("/{appointmentId}/note")
    public ResponseEntity<AppointmentResponse> addDoctorNote( // Dönüş tipi AppointmentResponse oldu
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody DoctorNoteRequest request
    ) {
        Appointment updatedAppointment = appointmentService.addDoctorNote(
                appointmentId,
                currentUser.getId(),
                request.getNotes()
        );
        // Entity'den DTO'ya dönüştür
        return ResponseEntity.ok(appointmentMapper.toAppointmentResponse(updatedAppointment));
    }

    /**
     * Hem hasta hem de doktorun kendi randevularını ID ile detaylı görmesini sağlar.
     * Güvenlik kontrolü (sadece randevuyla ilgili olan kişi görebilir) burada yapılır.
     *
     * @param appointmentId Randevunun ID'si
     * @param currentUser   Giriş yapan kullanıcının bilgileri
     * @return Randevu bilgisiyle HTTP 200 OK yanıtı
     */
    @PreAuthorize("hasAnyRole('HASTA', 'DOKTOR')") // Rol adları: PATIENT, DOCTOR -> HASTA, DOKTOR
    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> getAppointmentDetails( // Dönüş tipi AppointmentResponse oldu
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);

        // Görüntüleme yetkilendirmesi: Sadece randevunun hastası veya doktoru görebilir
        if (!appointment.getPatient().getId().equals(currentUser.getId()) &&
            !appointment.getDoctor().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bu randevuyu görüntüleme yetkiniz yok.");
        }

        // Entity'den DTO'ya dönüştür
        return ResponseEntity.ok(appointmentMapper.toAppointmentResponse(appointment));
    }

    /**
     * Hastaların randevu alabileceği tüm doktorları listeler.
     *
     * @return Tüm doktorların listesi (DoctorResponse DTO olarak)
     */
    @PreAuthorize("hasRole('HASTA')") // Sadece hastalar doktor listesini görebilir
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        // UserService'ten tüm doktor rolündeki User'ları al
        // UserMapper'daki toDoctorResponseList metodunu kullanarak DoctorResponse listesine dönüştür
        List<User> doctors = userService.getAllDoctors(); // UserService'de bu metodun olduğundan emin olun
        return ResponseEntity.ok(userMapper.toDoctorResponseList(doctors));
    }
}