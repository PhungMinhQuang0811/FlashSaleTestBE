package com.mp.flashsale.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(name = "response.ApiResponse",description = "Standard API response format")
public class ApiResponse<T>{
    /**
     * The code signifies the status of the request
     * By default, it would be 1000, means that the request is successfully resolved
     */
    @Builder.Default
    @Schema(description = "Response code", defaultValue = "1000")
    int code = 1000;

    /**
     * Short message inform client about the result of processing the request
     * By default, it would be "Success"
     */
    @Builder.Default
    @Schema(description = "Response message", defaultValue = "Success")
    String message = "Success";

    /**
     * An object which is data return for the client.
     */
    @Schema(description = "Data returned")
    T data;
}
