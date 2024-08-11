package ru.practicum.explorewithme.users.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewUserRequest {
    @NotNull
    @Email
    @Size(min = 6, max = 254)
    private String email;
    @NotNull
    @Size(min = 2, max = 250)
    private String name;
}
