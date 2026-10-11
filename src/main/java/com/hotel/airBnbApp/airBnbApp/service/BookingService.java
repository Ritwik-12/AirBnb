package com.hotel.airBnbApp.airBnbApp.service;

import com.hotel.airBnbApp.airBnbApp.dto.BookingDto;
import com.hotel.airBnbApp.airBnbApp.dto.BookingRequest;
import com.hotel.airBnbApp.airBnbApp.dto.GuestDto;
import com.hotel.airBnbApp.airBnbApp.dto.HotelReportDto;
import com.stripe.model.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookingService {


    BookingDto initializeBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);

    List<BookingDto> getAllBookingsByHotelID(Long hotelId);

    HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDto> getBookings();
}
