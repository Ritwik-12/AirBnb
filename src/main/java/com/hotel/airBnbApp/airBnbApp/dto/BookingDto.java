package com.hotel.airBnbApp.airBnbApp.dto;


import com.hotel.airBnbApp.airBnbApp.entity.Hotel;
import com.hotel.airBnbApp.airBnbApp.entity.Room;
import com.hotel.airBnbApp.airBnbApp.entity.User;
import com.hotel.airBnbApp.airBnbApp.entity.enums.BookingStatus;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingDto {


    private Long id;
    private Integer roomsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate chekInDate;
    private LocalDate checkOutDate;
    private BookingStatus bookingStatus;
    private Set<GuestDto> guests;
    private BigDecimal amount;
}
