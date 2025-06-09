package org.example.dto.request;

// request/AppointmentStatusUpdateRequest.java
import lombok.Data;
import org.example.enums.Status;

import jakarta.validation.constraints.NotNull;

@Data
public class AppointmentStatusUpdateRequest {
    @NotNull
    private Status newStatus;
}

