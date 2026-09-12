package com.hotel.airBnbApp.airBnbApp.dto;

import com.hotel.airBnbApp.airBnbApp.entity.HotelContactInfo;

import lombok.Data;


@Data
public class HotelDto {

    private Long id;
    private String name;
    private String city;
    private String[] photos;
    private String[] amenities;
    private HotelContactInfo contactInfo;
    private boolean active;
}
