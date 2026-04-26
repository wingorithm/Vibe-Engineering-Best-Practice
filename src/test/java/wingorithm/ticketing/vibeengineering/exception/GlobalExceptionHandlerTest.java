package wingorithm.ticketing.vibeengineering.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import wingorithm.ticketing.vibeengineering.common.model.dto.BaseResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    @Test
    void handleAllExceptions_returnsInternalServerErrorWithStandardSchema() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Exception ex = new Exception("Test exception message");

        ResponseEntity<BaseResponse<Object>> responseEntity = handler.handleAllExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        
        BaseResponse<Object> response = responseEntity.getBody();
        assertNotNull(response.getErrorSchema());
        assertEquals("9999", response.getErrorSchema().getErrorCode());
        assertEquals("Test exception message", response.getErrorSchema().getMessage());
        assertNull(response.getOutputSchema());
    }
}
