package com.hotel.airBnbApp.airBnbApp.dto;


import lombok.Data;

import java.util.List;

@Data
public class HotelInfoDto {



    private HotelDto hotel;

    private List<RoomDto> rooms;


    public HotelInfoDto(HotelDto map, List<RoomDto> rooms) {
        this.hotel=map;
        this.rooms=rooms;
    }
}
