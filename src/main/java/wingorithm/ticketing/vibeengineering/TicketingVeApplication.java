package wingorithm.ticketing.vibeengineering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TicketingVeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketingVeApplication.class, args);
    }

}
