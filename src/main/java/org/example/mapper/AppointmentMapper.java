package org.example.mapper; // Tek package tanımı

import org.example.dto.response.AppointmentResponse;
import org.example.model.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AppointmentMapper {
    @Mapping(source = "patient", target = "patient")
    @Mapping(source = "doctor", target = "doctor", qualifiedByName = "toDoctorResponse")
    AppointmentResponse toAppointmentResponse(Appointment appointment);
    List<AppointmentResponse> toAppointmentResponseList(List<Appointment> appointments);
}