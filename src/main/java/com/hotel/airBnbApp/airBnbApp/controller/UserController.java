package com.hotel.airBnbApp.airBnbApp.controller;


import com.hotel.airBnbApp.airBnbApp.dto.BookingDto;
import com.hotel.airBnbApp.airBnbApp.dto.ProfileUpdateRequestDto;
import com.hotel.airBnbApp.airBnbApp.dto.UserDto;
import com.hotel.airBnbApp.airBnbApp.service.BookingService;
import com.hotel.airBnbApp.airBnbApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookingService  bookingService;

    @PatchMapping("/profile")
    public ResponseEntity<Void>  updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto){
        userService.updateProfile(profileUpdateRequestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/myBookings")
    public ResponseEntity<List<BookingDto>> getMyBookings(){
        return ResponseEntity.ok(bookingService.getBookings());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getMyProfile(){
        return ResponseEntity.ok(userService.getMyProfile());
    }

    //need to write the guests controller
    //create guest , add guest , update and delete guests
}
