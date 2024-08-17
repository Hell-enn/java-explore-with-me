package ru.practicum.explorewithme.compilations.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.explorewithme.events.dto.EventShortDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    @NotNull
    private Long id;
    @NotNull
    private Boolean pinned;
    @NotNull
    @Size(min = 1)
    private String title;
    private List<EventShortDto> events;
}
