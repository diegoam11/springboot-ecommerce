package org.diego.ecommerce.demo.repositories;

import org.diego.ecommerce.demo.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA generates the implementation of the interface at runtime
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
