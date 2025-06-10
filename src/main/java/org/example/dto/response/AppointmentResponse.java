package org.example.dto.response; // Tek package tanımı

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.enums.Status;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private UserResponse patient;
    private DoctorResponse doctor;
    private LocalDateTime appointmentDateTime;
    private Status status;
    private String doctorNotes;
}