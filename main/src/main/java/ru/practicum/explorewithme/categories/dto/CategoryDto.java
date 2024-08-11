package ru.practicum.explorewithme.categories.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private Long id;
    @NotNull
    @Size(min = 1, max = 50)
    private String name;
}