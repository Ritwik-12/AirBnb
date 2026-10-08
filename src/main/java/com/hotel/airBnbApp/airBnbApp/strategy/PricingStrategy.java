package com.hotel.airBnbApp.airBnbApp.strategy;

import com.hotel.airBnbApp.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
