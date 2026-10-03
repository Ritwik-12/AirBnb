package com.hotel.airBnbApp.airBnbApp.service;

import com.hotel.airBnbApp.airBnbApp.dto.BookingDto;
import com.hotel.airBnbApp.airBnbApp.dto.BookingRequest;
import com.hotel.airBnbApp.airBnbApp.dto.GuestDto;
import com.hotel.airBnbApp.airBnbApp.entity.*;
import com.hotel.airBnbApp.airBnbApp.entity.enums.BookingStatus;
import com.hotel.airBnbApp.airBnbApp.exception.ResourceNotFoundException;
import com.hotel.airBnbApp.airBnbApp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{



    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public BookingDto initializeBooking(BookingRequest bookingRequest) {

        log.info("Initialzing booking for hotel: {},room {}, date {}-{}",bookingRequest.getHotelId(),
                bookingRequest.getRoomId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckoutDate());

        Hotel hotel=hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(()->
                new ResourceNotFoundException("Hotel not found with id: "+bookingRequest.getHotelId()));

        Room room =roomRepository.findById(bookingRequest.getRoomId()).orElseThrow(()->
            new ResourceNotFoundException("Room not found with id: "+bookingRequest.getRoomId()));

        List<Inventory> inventoryList=inventoryRepository.findAndLockAvailableInventory(room.getId(),
                  bookingRequest.getCheckInDate(),
                bookingRequest.getCheckoutDate(),
                bookingRequest.getRoomCount()
                );

        long daysCount= ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(),bookingRequest.getCheckoutDate())+1;

        if(inventoryList.size() !=daysCount){
            throw new IllegalStateException("Room is not available anymore");
        }

        //Reserve the room / updates the booked count of the inventory
        for(Inventory inventory:inventoryList){
                inventory.setReservedCount(inventory.getReservedCount()+ bookingRequest.getRoomCount());
        }

        inventoryRepository.saveAll(inventoryList);

        //create the booking


        //TODO: CALCULATE DYNAMIC PRICE

        Booking booking=Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .chekInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckoutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomCount())
                .amount(BigDecimal.TEN)
                .build();

        booking=bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests for booking with id: {}",bookingId);

        Booking booking= bookingRepository.findById(bookingId)
                .orElseThrow(()->
                    new ResourceNotFoundException("Booking not found with id: "+bookingId)
                );

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has alraedy expired");
        }

        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under reserved state , cannto add guests");
        }

        for(GuestDto guestDto: guestDtoList){
            Guest guest=modelMapper.map(guestDto, Guest.class);
            guest.setUser(getCurrentUser());
            guest=guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUEST_ADDED);
        booking=bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDto.class);
    }

    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser(){
        User user = new User();  //TODO: REMOVE DUMMY USER
        user.setId(1L);
        return user;
    }
}
