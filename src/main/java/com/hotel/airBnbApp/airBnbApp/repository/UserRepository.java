package com.hotel.airBnbApp.airBnbApp.repository;

import com.hotel.airBnbApp.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
}
