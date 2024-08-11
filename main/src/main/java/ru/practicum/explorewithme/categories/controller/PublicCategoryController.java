package ru.practicum.explorewithme.categories.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.categories.service.CategoryService;


@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class PublicCategoryController {

    private final CategoryService categoryServiceImpl;

    @GetMapping
    public ResponseEntity<Object> getCategories(@RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.debug("Принят запрос на получение категорий с позиции {} в количестве {}", from, size);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryServiceImpl.getCategories(from, size));
    }


    @GetMapping("/{catId}")
    public ResponseEntity<Object> getCategory(@PathVariable Long catId) {
        log.debug("Принят запрос на получение категории с id = {}", catId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryServiceImpl.getCategory(catId));
    }
}
