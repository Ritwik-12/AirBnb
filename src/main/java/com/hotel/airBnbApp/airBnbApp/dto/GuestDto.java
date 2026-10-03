package com.hotel.airBnbApp.airBnbApp.dto;

import com.hotel.airBnbApp.airBnbApp.entity.User;
import com.hotel.airBnbApp.airBnbApp.entity.enums.Gender;

import lombok.Data;


import java.time.LocalDateTime;

@Data
public class GuestDto {

    private Long id;
    private User user;
    private String name;
    private LocalDateTime createdAt;
    private Gender gender;
    private Integer age;
}
