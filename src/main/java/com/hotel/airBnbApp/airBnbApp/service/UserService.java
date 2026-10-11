package com.hotel.airBnbApp.airBnbApp.service;

import com.hotel.airBnbApp.airBnbApp.dto.ProfileUpdateRequestDto;
import com.hotel.airBnbApp.airBnbApp.dto.UserDto;
import com.hotel.airBnbApp.airBnbApp.entity.User;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
