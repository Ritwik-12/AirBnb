package com.hotel.airBnbApp.airBnbApp.service;

import com.hotel.airBnbApp.airBnbApp.dto.BookingDto;
import com.hotel.airBnbApp.airBnbApp.dto.BookingRequest;
import com.hotel.airBnbApp.airBnbApp.dto.GuestDto;
import com.hotel.airBnbApp.airBnbApp.entity.*;
import com.hotel.airBnbApp.airBnbApp.entity.enums.BookingStatus;
import com.hotel.airBnbApp.airBnbApp.exception.ResourceNotFoundException;
import com.hotel.airBnbApp.airBnbApp.exception.UnAuthorisedException;
import com.hotel.airBnbApp.airBnbApp.repository.*;
import com.hotel.airBnbApp.airBnbApp.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

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
    private final CheckoutService checkoutService;
    private final PricingService pricingService;


    @Value("${frontend.url}")
    private String frontendUrl;


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

//        //Reserve the room / updates the booked count of the inventory
//        for(Inventory inventory:inventoryList){
//                inventory.setReservedCount(inventory.getReservedCount()+ bookingRequest.getRoomCount());
//        }
//
//        inventoryRepository.saveAll(inventoryList);
//
//        //create the booking
        inventoryRepository.initBooking(room.getId(),bookingRequest.getCheckInDate(),
                bookingRequest.getCheckoutDate(), bookingRequest.getRoomCount());


        //TODO: CALCULATE DYNAMIC PRICE -DONE

        BigDecimal priceForOneRoom=pricingService.calculateTotalPrice(inventoryList);

        BigDecimal totalPrice=priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomCount()));


        Booking booking=Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .chekInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckoutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomCount())
                .amount(totalPrice)
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

        User user=getCurrentUser();


        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has alraedy expired");
        }

        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under reserved state , cannto add guests");
        }

        for(GuestDto guestDto: guestDtoList){
            Guest guest=modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest=guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUEST_ADDED);
        booking=bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDto.class);
    }

    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {

        Booking booking=bookingRepository.findById(bookingId).orElseThrow(
                ()->new ResourceNotFoundException("booking not found with id "+bookingId)
        );

        User user=getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has alraedy expired");
        }

        String sessionUrl=checkoutService.getCheckoutSession(booking,
                frontendUrl+"/payments/success", frontendUrl+"/payments/failure");

        booking.setStatus(BookingStatus.PAYMENTS_PENDING);
        bookingRepository.save(booking);
        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType())){
            Session session=(Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if(session ==null)return;

            String sessionId= session.getId();
            Booking booking=bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(
                    ()-> new ResourceNotFoundException("Booking not found for session Id: "+sessionId));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getChekInDate(),
                    booking.getCheckOutDate(),booking.getRoomsCount());


            inventoryRepository.confirmBooking(booking.getRoom().getId(),booking.getChekInDate(),
                            booking.getCheckOutDate() ,booking.getRoomsCount()
            );

            log.info("Successfully confirmed the booking for Booking Id: {}",booking.getId());

        }else{
            log.warn("Unhandled event type: {}",event.getType());
        }
    }

    @Override
    public void cancelBooking(Long bookingId) {
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(
                ()->new ResourceNotFoundException("booking not found with id "+bookingId)
        );

        User user=getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }

        if(booking.getBookingStatus()!=BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only confirmed booking can be cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getChekInDate(),
                booking.getCheckOutDate(),booking.getRoomsCount()
                );

        inventoryRepository.cancelBooking(booking.getRoom().getId(),booking.getChekInDate(),
                    booking.getCheckOutDate(),booking.getRoomsCount()
                );

        //handle the refund

        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams= RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())

                    .build();


            Refund.create(refundParams);
        }catch(StripeException e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(
                ()->new ResourceNotFoundException("booking not found with id "+bookingId)
        );

        User user=getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }

        return booking.getBookingStatus().name();
    }

    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser(){
       return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
