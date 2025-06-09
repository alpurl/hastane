package org.example.service;

import org.example.model.Appointment;
import org.example.model.User;
import org.example.repository.AppointmentRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.example.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

import org.example.enums.Role;
import org.example.exception.BadRequestException;
import org.example.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;



@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    // Constructor Injection ile bağımlılıkları enjekte ediyoruz
    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Yeni bir randevu oluşturur.
     * Hasta ve doktor rollerini, randevu çakışmasını kontrol eder.
     *
     * @param patientId   Randevuyu alan hastanın ID'si
     * @param doctorId    Randevunun alındığı doktorun ID'si
     * @param dateTime    Randevunun tarih ve saati
     * @return Oluşturulan Appointment nesnesi
     * @throws ResourceNotFoundException Hasta veya doktor bulunamazsa
     * @throws BadRequestException       Roller uygun değilse veya randevu çakışması varsa
     */
    @Transactional
    public Appointment createAppointment(Long patientId, Long doctorId, LocalDateTime dateTime) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Hasta bulunamadı: " + patientId));
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doktor bulunamadı: " + doctorId));

        // Rol kontrolü: Belirtilen ID'lerin doğru rollerde olduğundan emin olun
        if (patient.getRole() != Role.HASTA) {
            throw new BadRequestException("ID " + patientId + " bir hasta değil.");
        }
        if (doctor.getRole() != Role.DOKTOR) {
            throw new BadRequestException("ID " + doctorId + " bir doktor değil.");
        }

        // Randevu çakışması kontrolü: Aynı doktorun aynı saatte başka bir randevusu olmamalı
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateTime(doctorId, dateTime)) {
            throw new BadRequestException("Seçilen doktorun bu saatte zaten başka bir randevusu var.");
        }

        // Opsiyonel: Aynı hastanın aynı saatte başka bir randevusu olmamasını da engelleyebiliriz (FR2.2)
        // if (appointmentRepository.existsByPatientIdAndAppointmentDateTime(patientId, dateTime)) {
        //     throw new BadRequestException("Bu saatte zaten başka bir randevunuz var.");
        // }


        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDoctor(doctor);
        appt.setAppointmentDateTime(dateTime); // LocalDateTime alanının adı AppointmentDateTime olarak güncellendi
        appt.setStatus(Status.BEKLEMEDE); // Varsayılan durum "Beklemede"

        return appointmentRepository.save(appt);
    }

    /**
     * Belirli bir hastanın geçmiş randevularını listeler.
     * Not: "Geçmiş" randevular için randevu tarihi mevcut zamandan önce olmalı.
     * Bu metot sadece hastanın kendi geçmiş randevularını görebilmesini sağlamak için kullanılır.
     *
     * @param patientId Geçmiş randevuları istenen hastanın ID'si
     * @return Hastanın geçmiş randevularının listesi
     * @throws ResourceNotFoundException Hasta bulunamazsa
     * @throws BadRequestException       Belirtilen ID'nin hasta rolünde değilse
    */
    public List<Appointment> getPatientPastAppointments(Long patientId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Hasta bulunamadı: " + patientId));

        if (patient.getRole() != Role.HASTA) {
            throw new BadRequestException("ID " + patientId + " bir hasta değil.");
        }

        // Randevu tarihine göre filtreleme de eklenebilir, şimdilik sadece hastaya göre listeler
        // ve veritabanı sorgusunda sıralama yapar.
        return appointmentRepository.findByPatientIdOrderByAppointmentDateTimeDesc(patientId);
    } 

    /**
     * Belirli bir doktorun tüm randevularını listeler (Beklemede, Onaylandı, Reddedildi, Tamamlandı).
     *
     * @param doctorId Randevuları istenen doktorun ID'si
     * @return Doktorun tüm randevularının listesi
     * @throws ResourceNotFoundException Doktor bulunamazsa
     * @throws BadRequestException       Belirtilen ID'nin doktor rolünde değilse
    */
    public List<Appointment> getDoctorAppointments(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doktor bulunamadı: " + doctorId));

        if (doctor.getRole() != Role.DOKTOR) {
            throw new BadRequestException("ID " + doctorId + " bir doktor değil.");
        }
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateTimeAsc(doctorId);
    } 

    /**
     * Belirli bir doktorun beklemede olan randevu taleplerini listeler.
     *
     * @param doctorId Randevu talepleri istenen doktorun ID'si
     * @return Doktorun beklemede olan randevu taleplerinin listesi
     * @throws ResourceNotFoundException Doktor bulunamazsa
     * @throws BadRequestException       Belirtilen ID'nin doktor rolünde değilse
     */
    public List<Appointment> getDoctorPendingAppointments(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doktor bulunamadı: " + doctorId));

        if (doctor.getRole() != Role.DOKTOR) {
            throw new BadRequestException("ID " + doctorId + " bir doktor değil.");
        }
        return appointmentRepository.findByDoctorIdAndStatusOrderByAppointmentDateTimeAsc(doctorId, Status.BEKLEMEDE);
    }


    /**
     * Randevu durumunu günceller (Sadece doktorlar tarafından yapılabilir).
     *
     * @param appointmentId Güncellenecek randevunun ID'si
     * @param doctorId      Güncelleme yapan doktorun ID'si (yetki kontrolü için)
     * @param newStatus     Yeni durum
     * @return Güncellenen Appointment nesnesi
     * @throws ResourceNotFoundException Randevu bulunamazsa
     * @throws BadRequestException       Yetkilendirme sorunları veya geçersiz durum geçişi varsa
     */
    @Transactional
    public Appointment updateStatus(Long appointmentId, Long doctorId, Status newStatus) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı: " + appointmentId));

        // Randevuyu güncelleyen kişinin, randevunun doktoru olduğundan emin olun
        if (!appt.getDoctor().getId().equals(doctorId)) {
            throw new BadRequestException("Bu randevuyu güncelleme yetkiniz yok.");
        }

        // Durum geçiş kuralları
        // Örn: Reddedilmiş veya tamamlanmış bir randevunun durumu değiştirilemez.
        if (appt.getStatus() == Status.REDDEDILDI || appt.getStatus() == Status.TAMAMLANDI) {
            throw new BadRequestException("Reddedilmiş veya tamamlanmış bir randevunun durumu değiştirilemez.");
        }
        // Örn: Sadece BEKLEMEDE durumundaki randevular ONAYLANDI veya REDDEDILDI olabilir
        if (appt.getStatus() == Status.BEKLEMEDE &&
                (newStatus == Status.ONAYLANDI || newStatus == Status.REDDEDILDI)) {
            appt.setStatus(newStatus);
        } else if (appt.getStatus() == Status.ONAYLANDI && newStatus == Status.TAMAMLANDI) {
            // Onaylandı durumundaki randevular 'Tamamlandı' olabilir (genellikle doktor not ekledikten sonra)
            appt.setStatus(newStatus);
        } else {
            throw new BadRequestException("Geçersiz randevu durumu geçişi: " + appt.getStatus() + " -> " + newStatus);
        }

        return appointmentRepository.save(appt);
    }

    /**
     * Randevuya doktor notu ekler (Sadece doktorlar tarafından yapılabilir).
     * Genellikle randevu tamamlandıktan veya onaylandıktan sonra eklenir.
     * Not eklendiğinde randevunun durumu 'Tamamlandı' olarak güncellenebilir.
     *
     * @param appointmentId Not eklenecek randevunun ID'si
     * @param doctorId      Notu ekleyen doktorun ID'si (yetki kontrolü için)
     * @param note          Eklenecek not metni
     * @return Güncellenen Appointment nesnesi
     * @throws ResourceNotFoundException Randevu bulunamazsa
     * @throws BadRequestException       Yetkilendirme sorunları veya randevu durumu not eklemeye uygun değilse
     */
    @Transactional
    public Appointment addDoctorNote(Long appointmentId, Long doctorId, String note) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı: " + appointmentId));

        // Randevuyu güncelleyen kişinin, randevunun doktoru olduğundan emin olun
        if (!appt.getDoctor().getId().equals(doctorId)) {
            throw new BadRequestException("Bu randevuya not ekleme yetkiniz yok.");
        }

        // Not ekleme sadece onaylı veya tamamlanmış randevular için olabilir (FR2.3)
        if (appt.getStatus() != Status.ONAYLANDI && appt.getStatus() != Status.TAMAMLANDI) {
            throw new BadRequestException("Sadece onaylanmış veya tamamlanmış randevulara not eklenebilir.");
        }

        appt.setDoctorNotes(note); // Not alanı setDoctorNotes olarak güncellendi

        // Eğer randevu onaylı durumdaysa, not eklendikten sonra 'Tamamlandı' durumuna çekilebilir
        if (appt.getStatus() == Status.ONAYLANDI) {
            appt.setStatus(Status.TAMAMLANDI);
        }

        return appointmentRepository.save(appt);
    }

    /**
     * Belirli bir randevunun detaylarını getirir.
     * Bu metot, hem hasta hem de doktor tarafından randevu detaylarını görüntülemek için kullanılabilir.
     * Yetkilendirme (sadece ilgili hasta veya doktor görebilir) Controller katmanında veya burada yapılabilir.
     *
     * @param appointmentId Randevunun ID'si
     * @return Randevu nesnesi
     * @throws ResourceNotFoundException Randevu bulunamazsa
     */
    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı: " + appointmentId));
    }
}

