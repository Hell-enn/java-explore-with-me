package ru.practicum.explorewithme.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserShortDto {
    @NotBlank
    private Long id;
    @NotBlank
    @Size(min = 1)
    private String name;
}
