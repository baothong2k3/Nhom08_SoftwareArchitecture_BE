package iuh.fit.se.emailservice.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {
    private Long id;
    private String email;
    private String phoneNumber;
    private String shippingAddress;
    private String paymentMethod;
    private String createdAt;
    private BigDecimal totalPrice;
}
