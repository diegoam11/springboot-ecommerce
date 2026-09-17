package org.diego.ecommerce.demo.product;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        BigDecimal price,
        Integer stock,
        Long categoryId
) {
}
