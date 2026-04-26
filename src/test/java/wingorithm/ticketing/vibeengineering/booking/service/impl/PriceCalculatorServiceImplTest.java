package wingorithm.ticketing.vibeengineering.booking.service.impl;

import org.junit.jupiter.api.Test;
import wingorithm.ticketing.vibeengineering.customer.model.entity.CustomerEntity;
import wingorithm.ticketing.vibeengineering.customer.model.entity.CustomerTierEntity;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceCalculatorServiceImplTest {

    private final PriceCalculatorServiceImpl priceCalculatorService = new PriceCalculatorServiceImpl();

    @Test
    void calculateFinalPrice_LoversTier_ShouldApply30PercentDiscount() {
        EventEntity event = EventEntity.builder().basePrice(new BigDecimal("100.00")).build();
        CustomerTierEntity tier = CustomerTierEntity.builder().name("Lovers").discountPercentage(new BigDecimal("30")).build();
        CustomerEntity customer = CustomerEntity.builder().tier(tier).build();

        BigDecimal finalPrice = priceCalculatorService.calculateFinalPrice(event, customer, 1);
        
        assertEquals(new BigDecimal("70.00"), finalPrice);
    }
}