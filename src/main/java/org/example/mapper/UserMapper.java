package org.example.mapper;

import org.example.dto.response.DoctorResponse;
import org.example.dto.response.UserResponse;
import org.example.model.User; // Tek User entity'miz
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named; // MapStruct'ta metodlara isim vermek için

import java.util.List;

@Mapper(componentModel = "spring") // Spring context'ine bean olarak eklenmesini sağlar
public interface UserMapper {

    // User Entity'den UserResponse DTO'ya dönüştürme (Genel kullanıcı bilgisi)
    @Mapping(target = "specialty", source = "specialty") // Specialty alanı User'da olduğu için direkt maplenebilir
    UserResponse toUserResponse(User user);

    // User Entity'den DoctorResponse DTO'ya dönüştürme (Sadece doktorlara özgü özet bilgi)
    // Bu metod, input olarak bir User alır ve DoctorResponse'a çevirir.
    // MapStruct, alan adları eşleştiği sürece (firstName, lastName, specialty) otomatik mapler.
    @Named("toDoctorResponse") // Bu metoda özel bir isim veriyoruz, AppointmentMapper'da kullanacağız
    DoctorResponse toDoctorResponse(User user);

    // List<User>'dan List<DoctorResponse>'a dönüştürme (Doktor listesi için)
    // Sadece rolü DOCTOR olanları maplemek için manuel filtreleme gerekecek,
    // veya bu mantığı Service katmanında yapıp sadece DOCTOR rolündeki User'ları döndürüp burada mapleyebiliriz.
    // Şimdilik MapStruct'ın doğrudan liste dönüşümünü kullanalım, filtrelemeyi Service'te yapmanız daha mantıklı.
    List<DoctorResponse> toDoctorResponseList(List<User> doctors);
}