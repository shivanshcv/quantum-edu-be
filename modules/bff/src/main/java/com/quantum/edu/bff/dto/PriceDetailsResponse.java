package com.quantum.edu.bff.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceDetailsResponse {
    /**
     * Formatted list/MRP when there is a genuine sale; omitted otherwise.
     */
    private String originalPrice;
    /**
     * Formatted price the customer pays (sale amount when discounted, else single display price).
     */
    private String price;
}
