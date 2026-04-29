package wingorithm.ticketing.vibeengineering.booking.service.impl;

import org.springframework.stereotype.Service;
import wingorithm.ticketing.vibeengineering.booking.service.PriceCalculatorService;
import wingorithm.ticketing.vibeengineering.customer.model.entity.CustomerEntity;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PriceCalculatorServiceImpl implements PriceCalculatorService {

    @Override
    public BigDecimal calculateFinalPrice(EventEntity event, CustomerEntity customer, int quantity) {
        BigDecimal basePrice = event.getBasePrice();
        BigDecimal discount = customer.getTier().getDiscountPercentage().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal discountedPrice = basePrice.multiply(BigDecimal.ONE.subtract(discount));
        return discountedPrice.multiply(new BigDecimal(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
}