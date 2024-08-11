package ru.practicum.explorewithme.categories.mapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.categories.dto.CategoryDto;
import ru.practicum.explorewithme.categories.dto.NewCategoryDto;
import ru.practicum.explorewithme.categories.model.Category;
import ru.practicum.explorewithme.categories.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Component
@Slf4j
public class CategoryMapper {

    private final CategoryRepository categoryRepository;

    public Category newCategoryDtoToCategory(NewCategoryDto newCategoryDto) {
        return new Category(null, newCategoryDto.getName());
    }

    public CategoryDto categoryToCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }

    public List<CategoryDto> categoryToCategoryDtoList(Iterable<Category> categories) {
        List<CategoryDto> categoryDtos = new ArrayList<>();
        categories
                .forEach(category -> categoryDtos.add(new CategoryDto(category.getId(), category.getName())));
        return categoryDtos;
    }

    public Category updateCategory(Category category, NewCategoryDto newCategoryDto) {
        category.setName(newCategoryDto.getName());
        return category;
    }
}
