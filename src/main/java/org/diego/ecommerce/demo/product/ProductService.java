package org.diego.ecommerce.demo.product;

import org.diego.ecommerce.demo.category.Category;
import org.diego.ecommerce.demo.category.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository){
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductResponse> findAll(){
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ProductResponse create(ProductRequest productReq){
        return this.toResponse(productRepository.save(this.toProduct(productReq)));
    }

    private Product toProduct(ProductRequest request){
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.categoryId()));
        return new Product(request.name(), request.price(), request.stock(), category);
    }

    private ProductResponse toResponse(Product product){
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }
}
