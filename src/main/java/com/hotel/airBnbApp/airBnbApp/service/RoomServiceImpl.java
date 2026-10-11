package com.hotel.airBnbApp.airBnbApp.service;

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

import static com.hotel.airBnbApp.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;

    @Override
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {

        log.info("creating a new room in hotel {}" + hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel is not found with the id {} " + hotelId));

        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }

        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);
        // TODO: Create inventory as soon as room is created and hotel is active
        if (hotel.isActive()) {
            inventoryService.initalizeRoomForAYear(room);
        }
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all the rooms in hotle {} " + hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel with the id is not present {} " + hotelId));

        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }

        return hotel.getRooms()
                .stream()
                .map((room) -> modelMapper.map(room, RoomDto.class))
                .toList();
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room with id {} " + roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room with the id not found {} " + roomId));
        return modelMapper.map(room, RoomDto.class);
    }

    @Transactional
    @Override
    public void deleteRoomById(Long roomId) {

        log.info("Delete the room with id {} " + roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room with the id not found {} " + roomId));


        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(room.getHotel().getOwner())){
            throw new UnAuthorisedException("This user does not own this room with id: "+roomId);
        }

        // TODO: delete all future inventory for this room-done
        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);

    }

    @Override
    @Transactional
    public RoomDto updateRoomByid(Long hotelId, Long roomId, RoomDto roomDto) {
        log.info("Updating the room with the id {}", roomId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with the id " + hotelId));

        User user= getCurrentUser();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }

        Room room=roomRepository.findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with id {}"+roomId));

        modelMapper.map(roomDto,room);
        room.setId(roomId);

        //TODO: IF PRICE OF THE ROOM IS UPDATED , then update the inventory for this room

        room=roomRepository.save(room);
        return modelMapper.map(room, RoomDto.class);
    }
}
