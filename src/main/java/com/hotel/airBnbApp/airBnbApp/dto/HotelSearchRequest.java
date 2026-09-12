package com.hotel.airBnbApp.airBnbApp.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class HotelSearchRequest {

    private String city;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer roomCount;

    private Integer page;
    private Integer size = 10;

}
