package com.hotel.airBnbApp.airBnbApp.service;

import com.hotel.airBnbApp.airBnbApp.dto.HotelDto;
import com.hotel.airBnbApp.airBnbApp.dto.HotelInfoDto;
import com.hotel.airBnbApp.airBnbApp.dto.RoomDto;
import com.hotel.airBnbApp.airBnbApp.entity.Hotel;
import com.hotel.airBnbApp.airBnbApp.entity.Room;
import com.hotel.airBnbApp.airBnbApp.entity.User;
import com.hotel.airBnbApp.airBnbApp.exception.ResourceNotFoundException;
import com.hotel.airBnbApp.airBnbApp.exception.UnAuthorisedException;
import com.hotel.airBnbApp.airBnbApp.repository.HotelRepository;
import com.hotel.airBnbApp.airBnbApp.repository.RoomRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;
    private final RoomRepository roomRepository;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        // 1.convert this hotel dot to hotel entity
        log.info(" Creating a new hotel with the name ", hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false);

        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        hotel.setOwner(user);


        Hotel hotel1 = hotelRepository.save(hotel);
        log.info("Created a new hotel with the ID {}", hotel1.getId());
        return modelMapper.map(hotel1, HotelDto.class);

    }

    @Override
    public HotelDto getHotelById(Long hotelId) {

        log.info("Getting the hotel with the id {}", hotelId);

        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel with the id not found" + hotelId));

        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+user.getId());
        }

        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public List<HotelDto> getAllHotels() {

        log.info("Get all the hotels {}");
        List<Hotel> hotels = hotelRepository.findAll();
        return hotels.stream()
                .map((hotel) -> modelMapper.map(hotel, HotelDto.class)).toList();

    }

    @Override
    public HotelDto updateHotelById(HotelDto hotelDto, Long id) {
        log.info("Updeting the hotel with the id {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with the id " + id));

        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+user.getId());
        }

        modelMapper.map(hotelDto, hotel);
        hotel.setId(id);
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        log.info("Deleting the hotel by id {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel with the id not present " + id));

        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+user.getId());
        }

        // TODO: delete the future inventory of this hotel -done
        for (Room room : hotel.getRooms()) {
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());

        }
        hotelRepository.deleteById(id);

    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activate the hotel {} " + hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel with the id not present " + hotelId));
        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+user.getId());
        }
        hotel.setActive(true);
        // TODO:Create inventory for all the room of this hotel -done
        // assuming do it only once
        for (Room room : hotel.getRooms()) {
            inventoryService.initalizeRoomForAYear(room);
        }
    }


    //public method
    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel with the id not present " + hotelId));

        List<RoomDto> rooms=hotel.getRooms()
                .stream()
                .map((element)->modelMapper.map(element, RoomDto.class))
                .toList();
        return new HotelInfoDto(modelMapper.map(hotel,HotelDto.class),rooms);

    }
}
