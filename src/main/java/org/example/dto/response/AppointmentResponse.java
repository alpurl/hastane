package org.example.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import org.example.enums.Status; // Status enum'ınızın paketi

@Data
public class AppointmentResponse {
    private Long id;
    private UserResponse patient; // Patient bilgilerini UserResponse olarak döndür
    private DoctorResponse doctor; // Doctor bilgilerini DoctorResponse olarak döndür
    private LocalDateTime appointmentDateTime;
    private Status status;
    private String doctorNotes;
}
