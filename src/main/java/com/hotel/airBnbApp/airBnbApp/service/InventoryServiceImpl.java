package com.hotel.airBnbApp.airBnbApp.service;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import com.hotel.airBnbApp.airBnbApp.dto.HotelPriceDto;

import com.hotel.airBnbApp.airBnbApp.dto.InventoryDto;
import com.hotel.airBnbApp.airBnbApp.dto.UpdateInventoryRequestDto;
import com.hotel.airBnbApp.airBnbApp.entity.User;
import com.hotel.airBnbApp.airBnbApp.exception.ResourceNotFoundException;
import com.hotel.airBnbApp.airBnbApp.repository.HotelMinPriceRepository;
import com.hotel.airBnbApp.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;


import com.hotel.airBnbApp.airBnbApp.dto.HotelSearchRequest;
import com.hotel.airBnbApp.airBnbApp.entity.Inventory;
import com.hotel.airBnbApp.airBnbApp.entity.Room;
import com.hotel.airBnbApp.airBnbApp.repository.InventoryRepository;
import org.springframework.data.domain.Page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.hotel.airBnbApp.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final RoomRepository roomRepository;

    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final HotelMinPriceRepository hotelMinPriceRepository;

    @Override
    public void initalizeRoomForAYear(Room room) {

        log.info("Creating inventory for room {}");

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for (; !today.isAfter(endDate); today = today.plusDays(1)) {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteAllInventories(Room room) {

        log.info("Deleting the inventories of room id: {}",room.getId());
        inventoryRepository.deleteByRoom(room);
    }

    @Override
  public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        // create a pagable object

        log.info("Searching hotel for {} city, form {} to{}",hotelSearchRequest.getCity(),
                hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate()
        );

        PageRequest pageable = PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getSize());
        Long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate()) + 1;

        Page<HotelPriceDto> hotelPage =
                hotelMinPriceRepository.findHotelWithAvailableInventory(hotelSearchRequest.getCity(),
                hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate(),
                hotelSearchRequest.getRoomCount(),
                dateCount,
                pageable);

        return hotelPage;

    }

    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {

        log.info("Getting all inventory by room for room with id: {}",roomId);
        Room room =roomRepository.findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with id: {}"+roomId));

        User user=getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("You are not the owner of the room with id {}"+roomId);

        return inventoryRepository.findByRoomOrderByDate(room).stream()
                .map((element)->modelMapper.map(element,
                        InventoryDto.class))
                .collect(Collectors.toList());

    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {

        log.info("Updating all inventory by room for room with id: {} between data range {}- {}",roomId,
                updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate());
        Room room =roomRepository.findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with id: {}"+roomId));

        User user=getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("You are not the owner of the room with id {}"+roomId);


        inventoryRepository.getInventoryLockBeforeUpdate(roomId,updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate());

        inventoryRepository.updateInventory(roomId,updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate(),updateInventoryRequestDto.getClosed(),
                updateInventoryRequestDto.getSurgeFactor());








    }

}
