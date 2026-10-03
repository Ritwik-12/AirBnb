package com.hotel.airBnbApp.airBnbApp.controller;

import com.hotel.airBnbApp.airBnbApp.dto.HotelDto;
import com.hotel.airBnbApp.airBnbApp.dto.HotelInfoDto;
import com.hotel.airBnbApp.airBnbApp.service.HotelService;
import com.hotel.airBnbApp.airBnbApp.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hotel.airBnbApp.airBnbApp.dto.HotelSearchRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest) {
        Page<HotelDto> page = inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto>  getHotelInfo(@PathVariable Long hotelId){

        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }

}
