package org.example.mapper;

import org.example.dto.response.AppointmentResponse;
import org.example.model.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers; // MapStruct'ın otomatik olarak oluşturduğu mapper sınıflarını kullanmak için

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class}) // Bu mapper, UserMapper'ı dönüşümlerinde kullanacak
public interface AppointmentMapper {

    // Appointment Entity'den AppointmentResponse DTO'ya dönüştürme
    @Mapping(source = "patient", target = "patient") // patient alanı UserMapper.toUserResponse'a yönlendirilecek
    @Mapping(source = "doctor", target = "doctor", qualifiedByName = "toDoctorResponse") // doctor alanı UserMapper.toDoctorResponse'a yönlendirilecek
    AppointmentResponse toAppointmentResponse(Appointment appointment);

    // Appointment Entity listesinden AppointmentResponse DTO listesine dönüştürme
    List<AppointmentResponse> toAppointmentResponseList(List<Appointment> appointments);
}