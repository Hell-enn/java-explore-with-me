package ru.practicum.explorewithme.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

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
