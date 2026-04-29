package wingorithm.ticketing.vibeengineering.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import wingorithm.ticketing.vibeengineering.common.model.dto.BaseResponse;
import wingorithm.ticketing.vibeengineering.common.model.dto.ErrorSchema;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleAllExceptions(Exception ex) {
        log.error("An unexpected error occurred", ex);
        BaseResponse<Object> response = BaseResponse.builder()
                .errorSchema(ErrorSchema.builder()
                        .errorCode("9999")
                        .message("An unexpected error occurred")
                        .build())
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
