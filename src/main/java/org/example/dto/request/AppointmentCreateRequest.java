package org.example.dto.request;

// request/AppointmentCreateRequest.java
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class AppointmentCreateRequest {
    @NotNull
    private Long doctorId;
    @NotNull
    private LocalDateTime appointmentDateTime;
}
