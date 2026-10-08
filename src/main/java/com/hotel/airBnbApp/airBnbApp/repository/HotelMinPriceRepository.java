package com.hotel.airBnbApp.airBnbApp.repository;

import com.hotel.airBnbApp.airBnbApp.dto.HotelPriceDto;
import com.hotel.airBnbApp.airBnbApp.entity.Hotel;
import com.hotel.airBnbApp.airBnbApp.entity.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.util.Optional;

public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice,Long> {


    @Query("""
            SELECT new com.hotel.airBnbApp.airBnbApp.dto.HotelPriceDto(i.hotel,AVG(i.price))
            FROM HotelMinPrice i
            WHERE i.hotel.city= :city
            AND i.date BETWEEN :startDate AND :endDate
            AND i.hotel.active=true
            GROUP BY i.hotel
            """)
    Page<HotelPriceDto> findHotelWithAvailableInventory(

            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            PageRequest pageable

    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}
