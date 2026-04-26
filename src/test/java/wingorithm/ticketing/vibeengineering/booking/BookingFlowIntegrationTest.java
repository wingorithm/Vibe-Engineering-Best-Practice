// src/test/java/wingorithm/ticketing/vibeengineering/booking/BookingFlowIntegrationTest.java
package wingorithm.ticketing.vibeengineering.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import wingorithm.ticketing.vibeengineering.booking.model.dto.ReserveTicketRequest;
import wingorithm.ticketing.vibeengineering.booking.repository.BookingRepository;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;
import wingorithm.ticketing.vibeengineering.event.repository.EventRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test") // Assuming you have a test profile to potentially connect to a test DB
class BookingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void reserveTicket_EndToEnd_Success() throws Exception {
        // Find an event from seed data
        EventEntity event = eventRepository.findAll().get(0);

        ReserveTicketRequest request = new ReserveTicketRequest();
        request.setEventId(event.getId());
        request.setCustomerId(UUID.fromString("c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1")); // From V2 seed
        request.setQuantity(1);
        String idempotencyKey = UUID.randomUUID().toString();

        int initialTickets = event.getAvailableTickets();

        mockMvc.perform(post("/api/v1/bookings")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorSchema.errorCode").value("0000"))
                .andExpect(jsonPath("$.outputSchema.status").value("RESERVED"));

        EventEntity updatedEvent = eventRepository.findById(event.getId()).get();
        assertEquals(initialTickets - 1, updatedEvent.getAvailableTickets());
    }
}
