package com.hotel.airBnbApp.airBnbApp.dto;

import com.hotel.airBnbApp.airBnbApp.entity.Hotel;
import com.hotel.airBnbApp.airBnbApp.entity.Room;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InventoryDto {


    private Long id;
    private LocalDate date;
    private Integer bookedCount;
    private int reservedCount;
    private Integer totalCount;
    private BigDecimal surgeFactor;
    private BigDecimal price;
    private Boolean closed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
