package wingorithm.ticketing.vibeengineering.exception;

public class IdempotencyException extends RuntimeException {
    public IdempotencyException(String message) {
        super(message);
    }
}
