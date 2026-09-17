package org.diego.ecommerce.demo.product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stock,
        // category
        Long categoryId,
        String categoryName
) {
}
