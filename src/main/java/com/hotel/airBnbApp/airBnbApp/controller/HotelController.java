package com.hotel.airBnbApp.airBnbApp.controller;

import com.hotel.airBnbApp.airBnbApp.dto.BookingDto;
import com.hotel.airBnbApp.airBnbApp.dto.HotelDto;
import com.hotel.airBnbApp.airBnbApp.dto.HotelReportDto;
import com.hotel.airBnbApp.airBnbApp.service.BookingService;
import com.hotel.airBnbApp.airBnbApp.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(path="/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {


    private final HotelService hotelService;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto){

        log.info("Creating a new hotel {}");
        HotelDto hotel= hotelService.createNewHotel(hotelDto);
        return new ResponseEntity<>(hotel, HttpStatus.CREATED);

    }
    @GetMapping(path="/{id}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long  id){
         log.info("Getting the hotel with the id");
           HotelDto hotel= hotelService.getHotelById(id);
           return ResponseEntity.ok(hotel);
    }

//    @GetMapping
//    public ResponseEntity<List<HotelDto>> getAllHotels(){
//       List<HotelDto> hotels= hotelService.getAllHotels();
//       return  ResponseEntity.ok(hotels);
//    }

    @PutMapping(path="/{hotelId}")
    public ResponseEntity<HotelDto> updateHotelById(@RequestBody HotelDto hotelDto,@PathVariable Long hotelId){
            log.info("updeting the hotel with the id {}");
            HotelDto hotel=hotelService.updateHotelById(hotelDto,hotelId);
            return ResponseEntity.ok(hotel);

    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId){
        hotelService.deleteHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{hotelId}/activate")
    public ResponseEntity<Void> activateHotel(@PathVariable Long hotelId){
        hotelService.activateHotel(hotelId);
        return ResponseEntity.noContent().build();

    }

    @GetMapping
    public ResponseEntity<List<HotelDto>> getAllHotelsOfaUser(){
        return ResponseEntity.ok(hotelService.getAllHotelsOfaUser());
    }

    @GetMapping("{hotelId}/bookings")
    public ResponseEntity<List<BookingDto>> getAllBookingsByHotelID(@PathVariable Long hotelId){
        return ResponseEntity.ok(bookingService.getAllBookingsByHotelID(hotelId));
    }

    @GetMapping("{hotelId}/reports")
    public ResponseEntity<HotelReportDto> getHotelReport(@PathVariable Long hotelId,
                                                         @RequestParam(required=false) LocalDate startDate,
                                                         @RequestParam(required=false)  LocalDate endDate

    ){

        if(startDate==null){
            startDate=LocalDate.now().minusMonths(1);
        }
        if(endDate==null){
            endDate=LocalDate.now();
        }
        return ResponseEntity.ok(bookingService.getHotelReport(hotelId,startDate,endDate));
    }

}
