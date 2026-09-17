package org.diego.ecommerce.demo.category;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService  categoryService){
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> getAll(){
        return categoryService.findAll();
    }

    @PostMapping
    public CategoryResponse create(@RequestBody CategoryRequest category){
        return categoryService.create(category);
    }
}
