package com.hotel.airBnbApp.airBnbApp.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="hotel_id",nullable=false)
    private Hotel hotle;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(nullable=false,name="room_id")
    private Room room;


    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",nullable=false)
    private User user;

    @Column(nullable = false)
    private Integer roomsCount;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="payment_id")
    private Payment payment;

    @CreationTimestamp
    private LocalDateTime creatdAt;


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

}
