package ru.practicum.explorewithme.compilations.dto;

import lombok.*;
import ru.practicum.explorewithme.events.dto.EventShortDto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
