package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.Status;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Appointment {

    @Id
    @GeneratedValue
    private Long id; // Her randevunun benzersiz ID’si

    @ManyToOne
    private User patient; // Randevuyu alan hasta

    @ManyToOne
    private User doctor; // Randevuyu alan doktora ait

    private LocalDateTime dateTime; // Randevu tarihi ve saati

    @Enumerated(EnumType.STRING)
    private Status status; // Randevunun durumu: BEKLEMEDE, ONAYLANDI, REDDEDILDI

    private String note; // Doktorun randevuya eklediği not

    // getter ve setter metotları
}
