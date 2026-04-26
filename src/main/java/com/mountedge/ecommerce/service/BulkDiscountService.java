package com.mountedge.ecommerce.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class BulkDiscountService {

    /**
     * Calculates the discount percentage based on the quantity of a single product line.
     * Tiers:
     * 5 - 9: 5%
     * 10 - 19: 10%
     * 20 - 49: 15%
     * 50+: 20%
     * 
     * @param quantity The quantity of the specific item.
     * @return The discount percentage as a BigDecimal (e.g., 5.00 for 5%).
     */
    public BigDecimal calculateDiscountPct(int quantity) {
        if (quantity >= 50) return new BigDecimal("20.00");
        if (quantity >= 20) return new BigDecimal("15.00");
        if (quantity >= 10) return new BigDecimal("10.00");
        if (quantity >= 5) return new BigDecimal("5.00");
        return BigDecimal.ZERO;
    }

    /**
     * Checks if a specific item quantity qualifies for any bulk discount.
     */
    public boolean qualifiesForBulkDiscount(int quantity) {
        return quantity >= 5;
    }
}
