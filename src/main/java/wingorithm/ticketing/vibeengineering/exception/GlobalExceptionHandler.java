package wingorithm.ticketing.vibeengineering.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import wingorithm.ticketing.vibeengineering.common.model.dto.BaseResponse;
import wingorithm.ticketing.vibeengineering.common.model.dto.ErrorSchema;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleAllExceptions(Exception ex) {
        BaseResponse<Object> response = BaseResponse.builder()
                .errorSchema(ErrorSchema.builder()
                        .errorCode("9999")
                        .message(ex.getMessage())
                        .build())
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
