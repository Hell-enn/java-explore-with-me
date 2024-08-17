package ru.practicum.explorewithme.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.users.dto.UserDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionDto {
    private Long id;
    @NotNull
    private UserDto follower;
    @NotNull
    private UserDto followed;
}