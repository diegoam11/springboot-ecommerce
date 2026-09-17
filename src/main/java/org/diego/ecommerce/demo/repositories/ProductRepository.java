package org.diego.ecommerce.demo.repositories;

import org.diego.ecommerce.demo.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
