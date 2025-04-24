/*
 * @ (#) AddressRequest.java    1.0    24/04/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package bookstore.userservice.dtos;/*
 * @description:
 * @author: Bao Thong
 * @date: 24/04/2025
 * @version: 1.0
 */

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {
    @NotBlank(message = "Address is required")
    private String address;

    private Long userId;
}
