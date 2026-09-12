package com.hotel.airBnbApp.airBnbApp.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.hotel.airBnbApp.airBnbApp.entity.Hotel;
import org.modelmapper.ModelMapper;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.hotel.airBnbApp.airBnbApp.dto.HotelDto;
import com.hotel.airBnbApp.airBnbApp.dto.HotelSearchRequest;
import com.hotel.airBnbApp.airBnbApp.entity.Inventory;
import com.hotel.airBnbApp.airBnbApp.entity.Room;
import com.hotel.airBnbApp.airBnbApp.repository.InventoryRepository;
import org.springframework.data.domain.Page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;

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

        inventoryRepository.deleteByRoom(room);
    }

    @Override
  public Page<HotelDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        // create a pagable object
        PageRequest pageable = PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getSize());
        Long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate()) + 1;

        Page<Hotel> hotelPage = inventoryRepository.findHotelWithAvailableInventory(hotelSearchRequest.getCity(),
                hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate(),
                hotelSearchRequest.getRoomCount(),
                dateCount,
                pageable);

        return hotelPage.map((element) -> modelMapper.map(element, HotelDto.class));

    }

}
