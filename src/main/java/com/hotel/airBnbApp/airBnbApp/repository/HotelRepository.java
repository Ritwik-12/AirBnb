package com.hotel.airBnbApp.airBnbApp.repository;

import com.hotel.airBnbApp.airBnbApp.entity.Hotel;
import com.hotel.airBnbApp.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel,Long> {
    List<Hotel> findByOwner(User user);
}
