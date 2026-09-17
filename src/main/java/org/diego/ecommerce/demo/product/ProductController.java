package org.diego.ecommerce.demo.product;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getAll(){
        return productService.findAll();
    }

    @PostMapping
    public ProductResponse create(@RequestBody ProductRequest request){
        return productService.create(request);
    }
}
