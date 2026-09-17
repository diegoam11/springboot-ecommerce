package org.diego.ecommerce.demo.category;

import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA generates the implementation of the interface at runtime
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
