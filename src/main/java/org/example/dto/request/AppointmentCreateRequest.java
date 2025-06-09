package org.example.dto.request;

// request/AppointmentCreateRequest.java
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class AppointmentCreateRequest {
    @NotNull
    private Long doctorId;
    @NotNull
    private LocalDateTime appointmentDateTime;
}
