package com.hotel.airBnbApp.airBnbApp.strategy;

import com.hotel.airBnbApp.airBnbApp.entity.Inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


//@Service
//@RequiredArgsConstructor
public class BasePricingStrategy implements PricingStrategy{



    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        return inventory.getRoom().getBasePrice();
    }
}
