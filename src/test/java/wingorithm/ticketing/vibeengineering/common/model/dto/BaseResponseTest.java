package wingorithm.ticketing.vibeengineering.common.model.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BaseResponseTest {

    @Test
    void success_createsResponseWithSuccessErrorSchemaAndOutput() {
        String testOutput = "Test Data";
        
        BaseResponse<String> response = BaseResponse.success(testOutput);
        
        assertNotNull(response.getErrorSchema());
        assertEquals("0000", response.getErrorSchema().getErrorCode());
        assertEquals("Success", response.getErrorSchema().getMessage());
        assertEquals(testOutput, response.getOutputSchema());
    }
}
