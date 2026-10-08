package com.hotel.airBnbApp.airBnbApp.service;

import com.hotel.airBnbApp.airBnbApp.dto.HotelDto;
import com.hotel.airBnbApp.airBnbApp.dto.HotelPriceDto;
import com.hotel.airBnbApp.airBnbApp.dto.HotelSearchRequest;
import com.hotel.airBnbApp.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {

    void initalizeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

}
