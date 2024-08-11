package ru.practicum.explorewithme.users.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
