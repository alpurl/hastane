package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.Status;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue
    private Long id; // Her randevunun benzersiz ID’si

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient; // Randevuyu alan hasta

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor; // Randevuyu alan doktora ait

    @Column(nullable = false)
    private LocalDateTime appointmentDateTime;// Randevu tarihi ve saati

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; // Randevunun durumu: BEKLEMEDE, ONAYLANDI, REDDEDILDI

    @Column(columnDefinition = "TEXT") // Daha uzun metinler için
    private String doctorNotes; // Doktorun randevuya eklediği not

    // getter ve setter metotları

    // Randevu oluşturulurken varsayılan status
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = Status.BEKLEMEDE;
        }
    }
}
