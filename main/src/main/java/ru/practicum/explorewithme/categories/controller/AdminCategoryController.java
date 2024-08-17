package ru.practicum.explorewithme.categories.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.categories.dto.NewCategoryDto;
import ru.practicum.explorewithme.categories.service.CategoryService;


@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class AdminCategoryController {
    private final CategoryService categoryServiceImpl;

    @PostMapping
    public ResponseEntity<Object> postCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.debug("Принят запрос на добавление новой категории \"{}\"", newCategoryDto.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryServiceImpl.addCategory(newCategoryDto));
    }


    @PatchMapping("/{catId}")
    public ResponseEntity<Object> patchCategory(@Valid @RequestBody NewCategoryDto newCategoryDto,
                                                @PathVariable Long catId) {
        log.debug("Принят запрос на обновление объекта категории с id = {} на {}", catId, newCategoryDto.getName());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryServiceImpl.patchCategory(catId, newCategoryDto));
    }


    @DeleteMapping("/{catId}")
    public ResponseEntity<Object> deleteCategory(@PathVariable Long catId) {
        log.debug("Принят запрос на удаление объекта категории с id = {}", catId);
        categoryServiceImpl.deleteCategory(catId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
