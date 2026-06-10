package org.settlehub.booking.core.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for receiving room booking requests from the client/API Gateway.
 */
@Data
public class BookRoomRequest {
    
    // FIXME: 
    // Temporary accepting the userId in the request body.
    // Later, once we've set up Security, we'll retrieve it from the JWT token.

    private Long userId; 
    
    private Long categoryId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String guestRequest;
}