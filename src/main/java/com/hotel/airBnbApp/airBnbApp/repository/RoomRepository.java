package com.hotel.airBnbApp.airBnbApp.repository;

import com.hotel.airBnbApp.airBnbApp.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room,Long> {
}
