package ru.practicum.explorewithme.compilations.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.compilations.service.CompilationService;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicCompilationController {

    private final CompilationService compilationServiceImpl;

    @GetMapping
    public ResponseEntity<Object> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                  @RequestParam(defaultValue = "0") Integer from,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        log.debug("Принят запрос на получение подборок событий с позиции {} в количестве {}", from, size);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(compilationServiceImpl.getCompilations(pinned, from, size));
    }

    @GetMapping("/{compId}")
    public ResponseEntity<Object> getCompilation(@PathVariable Long compId) {
        log.debug("Принят запрос на получение подборки событий с id = {}", compId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(compilationServiceImpl.getCompilation(compId));
    }
}
