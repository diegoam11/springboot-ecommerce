package org.diego.ecommerce.demo.category;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> findAll(){
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CategoryResponse create(CategoryRequest category){
        return this.toResponse(categoryRepository.save(new Category(category.name())));
    }

    private CategoryResponse toResponse(Category c){
        return new CategoryResponse(c.getId(), c.getName());
    }
}
