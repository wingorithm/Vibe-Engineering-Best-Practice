package wingorithm.ticketing.vibeengineering.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private ErrorSchema errorSchema;
    private T outputSchema;

    public static <T> BaseResponse<T> success(T output) {
        return BaseResponse.<T>builder()
                .errorSchema(ErrorSchema.builder()
                        .errorCode("0000")
                        .message("Success")
                        .build())
                .outputSchema(output)
                .build();
    }
}
