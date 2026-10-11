package com.hotel.airBnbApp.airBnbApp.entity;


import com.hotel.airBnbApp.airBnbApp.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="hotel_id",nullable=false)
    private Hotel hotel;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(nullable=false,name="room_id")
    private Room room;


    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",nullable=false)
    private User user;

    @Column(nullable = false)
    private Integer roomsCount;



    @CreationTimestamp
    private LocalDateTime createdAt;


    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private BookingStatus status;


    @Column(nullable = false)
    private LocalDate chekInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus bookingStatus;


    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(
            name="booking_guest",
            joinColumns=@JoinColumn(name="guest_id")

    )
    private Set<Guest> guests;

    @Column(nullable = false,precision = 10,scale = 2)
    private BigDecimal amount;


    @Column(unique=true)
    private String paymentSessionId;

}
