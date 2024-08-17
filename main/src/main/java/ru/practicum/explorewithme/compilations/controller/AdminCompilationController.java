package ru.practicum.explorewithme.compilations.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.compilations.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilations.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.compilations.service.CompilationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminCompilationController {

    private final CompilationService compilationServiceImpl;

    @PostMapping
    public ResponseEntity<Object> postCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.debug("Принят запрос на публикацию подборки событий с id = {} с названием \"{}\"",
                newCompilationDto.getEvents(), newCompilationDto.getTitle());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(compilationServiceImpl.postCompilation(newCompilationDto));
    }

    @DeleteMapping("/{compId}")
    public ResponseEntity<Object> deleteCompilation(@PathVariable Long compId) {
        log.debug("Принят запрос на удаление подборки событий с id = {}", compId);
        compilationServiceImpl.deleteCompilation(compId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PatchMapping("/{compId}")
    public ResponseEntity<Object> patchCompilation(@PathVariable Long compId,
                                                   @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        log.debug("Принят запрос на обновление подборки событий с id = {} объектом:" +
                "\n\tevents: " + updateCompilationRequest.getEvents() +
                "\n\tpinned: " + updateCompilationRequest.getPinned() +
                "\n\ttitle: " + updateCompilationRequest.getTitle(), compId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(compilationServiceImpl.patchCompilation(compId, updateCompilationRequest));
    }
}
