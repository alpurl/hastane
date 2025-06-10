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

import jakarta.validation.Valid;
import org.example.dto.request.AppointmentCreateRequest;
import org.example.dto.request.AppointmentStatusUpdateRequest;
import org.example.dto.request.DoctorNoteRequest;
import org.example.dto.response.AppointmentResponse;
import org.example.dto.response.DoctorResponse;
import org.example.exception.BadRequestException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.example.security.UserPrincipal;
import org.example.mapper.AppointmentMapper;
import org.example.mapper.UserMapper;


@RestController
@RequestMapping("/api/appointments") // Bu endpoint randevular için, doktor listesi için ayrı bir controller düşünebiliriz.
// Eğer /api/appointments/doctors endpoint'i bu controller'da kalacaksa sorun yok.
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final AppointmentMapper appointmentMapper;
    private final UserMapper userMapper;

    public AppointmentController(AppointmentService appointmentService,
                                 UserService userService,
                                 AppointmentMapper appointmentMapper,
                                 UserMapper userMapper) {
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.appointmentMapper = appointmentMapper;
        this.userMapper = userMapper;
    }

    @PreAuthorize("hasRole('HASTA')")
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody AppointmentCreateRequest request
    ) {
        Appointment newAppointment = appointmentService.createAppointment(
                currentUser.getId(),
                request.getDoctorId(),
                request.getAppointmentDateTime()
        );
        return new ResponseEntity<>(appointmentMapper.toAppointmentResponse(newAppointment), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('HASTA')")
    @GetMapping("/patient/past")
    public ResponseEntity<List<AppointmentResponse>> getPatientPastAppointments(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<Appointment> appointments = appointmentService.getPatientPastAppointments(currentUser.getId());
        return ResponseEntity.ok(appointmentMapper.toAppointmentResponseList(appointments));
    }

    @PreAuthorize("hasRole('DOKTOR')")
    @GetMapping("/doctor/all")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAllAppointments(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<Appointment> appointments = appointmentService.getDoctorAppointments(currentUser.getId());
        return ResponseEntity.ok(appointmentMapper.toAppointmentResponseList(appointments));
    }

    @PreAuthorize("hasRole('DOKTOR')")
    @GetMapping("/doctor/pending")
    public ResponseEntity<List<AppointmentResponse>> getDoctorPendingAppointments(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<Appointment> appointments = appointmentService.getDoctorPendingAppointments(currentUser.getId());
        return ResponseEntity.ok(appointmentMapper.toAppointmentResponseList(appointments));
    }


    @PreAuthorize("hasRole('DOKTOR')")
    @PutMapping("/{appointmentId}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody AppointmentStatusUpdateRequest request
    ) {
        // Hata düzeltmesi: appointmentService.updateStatus yerine appointmentService.updateAppointmentStatus çağrıldı
        // Not: Service katmanındaki metod adının doğru olduğunu varsayıyorum.
        Appointment updatedAppointment = appointmentService.updateAppointmentStatus( // <-- Burası düzeltildi
                appointmentId,
                currentUser.getId(),
                request.getNewStatus()
        );
        return ResponseEntity.ok(appointmentMapper.toAppointmentResponse(updatedAppointment));
    }

    @PreAuthorize("hasRole('DOKTOR')")
    @PutMapping("/{appointmentId}/note")
    public ResponseEntity<AppointmentResponse> addDoctorNote(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody DoctorNoteRequest request
    ) {
        Appointment updatedAppointment = appointmentService.addDoctorNote(
                appointmentId,
                currentUser.getId(),
                request.getNotes()
        );
        return ResponseEntity.ok(appointmentMapper.toAppointmentResponse(updatedAppointment));
    }

    @PreAuthorize("hasAnyRole('HASTA', 'DOKTOR')")
    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> getAppointmentDetails(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        // Burada Appointment entity'si çekiliyor ve sonra yetki kontrolü yapılıyor.
        // Bu, DTO dönüşümü Service katmanında yapılırsa daha uygun olabilir.
        // Örneğin: appointmentService.getAppointmentDetailsDto(appointmentId) metodunu çağırıp direkt DTO alabilirsiniz.
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);

        if (!appointment.getPatient().getId().equals(currentUser.getId()) &&
            !appointment.getDoctor().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bu randevuyu görüntüleme yetkiniz yok.");
        }

        return ResponseEntity.ok(appointmentMapper.toAppointmentResponse(appointment));
    }

    /**
     * Hastaların randevu alabileceği tüm doktorları listeler.
     * Bu endpoint, UserService'ten doktorları çekip DoctorResponse DTO'ya dönüştürür.
     * Bu endpoint'i "/api/users/doctors" gibi ayrı bir User/DoctorController'da tutmak daha temiz bir API tasarımı olabilir.
     * Ancak, bu controller'da kalmasını istediyseniz, burada da çalışacaktır.
     *
     * @return Tüm doktorların listesi (DoctorResponse DTO olarak)
     */
    @PreAuthorize("hasRole('HASTA')")
    @GetMapping("/doctors") // /api/appointments/doctors
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        List<User> doctors = userService.getAllDoctors(); // UserService'de bu metodun olduğundan emin olun
        return ResponseEntity.ok(userMapper.toDoctorResponseList(doctors)); // <-- toDoctorResponseList metodunu UserMapper'a ekleyeceğiz
    }
}