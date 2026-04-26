package wingorithm.ticketing.vibeengineering.payment.service;

import wingorithm.ticketing.vibeengineering.booking.model.entity.BookingEntity;
import wingorithm.ticketing.vibeengineering.payment.integration.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse processPayment(BookingEntity booking);
}
