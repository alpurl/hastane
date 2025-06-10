package org.example.dto.response; // Tek package tanımı

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String specialty;
}