package wingorithm.ticketing.vibeengineering.booking.service;

import wingorithm.ticketing.vibeengineering.customer.model.entity.CustomerEntity;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;
import java.math.BigDecimal;

public interface PriceCalculatorService {
    BigDecimal calculateFinalPrice(EventEntity event, CustomerEntity customer, int quantity);
}