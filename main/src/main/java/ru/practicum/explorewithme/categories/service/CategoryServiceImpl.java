package ru.practicum.explorewithme.categories.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.categories.dto.CategoryDto;
import ru.practicum.explorewithme.categories.dto.NewCategoryDto;
import ru.practicum.explorewithme.categories.mapper.CategoryMapper;
import ru.practicum.explorewithme.categories.model.Category;
import ru.practicum.explorewithme.categories.repository.CategoryRepository;
import ru.practicum.explorewithme.exceptions.ConflictException;
import ru.practicum.explorewithme.exceptions.NotFoundException;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        CategoryDto categoryDto = categoryMapper.categoryToCategoryDto(
                categoryRepository.save(categoryMapper.newCategoryDtoToCategory(newCategoryDto)));
        log.debug("Публикация категории прошла успешно!\n\t{}", categoryDto);
        return categoryDto;
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория события с id = " + catId + " не найдена!"));

        int events = categoryRepository.findEventsAmountByCategory(catId);
        if (events > 0)
            throw new ConflictException("Попытка удаления категории, к которой привязаны события!");

        categoryRepository.deleteById(catId);
        log.debug("Удаление категории с id = {} прошло успешно!", catId);
    }

    @Override
    @Transactional
    public CategoryDto patchCategory(Long catId, NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория события с id = " + catId + " не найдена!"));

        Category categoryWithSameName = categoryRepository.findByName(newCategoryDto.getName());
        if (categoryWithSameName != null && !categoryWithSameName.getId().equals(catId))
            throw new ConflictException("Категория с таким названием уже существует!");

        CategoryDto patchedCategoryDto = categoryMapper.categoryToCategoryDto(
                categoryMapper.updateCategory(category, newCategoryDto));
        log.debug("Обновление категории прошло успешно!\n\t{}", patchedCategoryDto);

        return patchedCategoryDto;
    }


    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        int amountOfRequests = categoryRepository.findAmount();
        int pageNum = amountOfRequests > from ? from / size : 0;

        Pageable page = PageRequest
                .of(pageNum, size)
                .toOptional()
                .orElseThrow(() -> new RuntimeException("Ошибка преобразования страницы!"));

        List<CategoryDto> categoryDtos = categoryMapper.categoryToCategoryDtoList(categoryRepository.findAll(page));
        log.debug("Возвращение списка категорий\n\t{}", categoryDtos);

        return categoryDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategory(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория события с id = " + catId + " не найдена!"));

        CategoryDto categoryDto = categoryMapper.categoryToCategoryDto(category);
        log.debug("Возвращение категории с id = {}", catId);

        return categoryDto;
    }
}
