package org.example.dto.request;

// request/DoctorNoteRequest.java
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class DoctorNoteRequest {
    @NotBlank
    private String notes;
}