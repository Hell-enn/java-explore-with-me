package ru.practicum.explorewithme.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.explorewithme.categories.dto.CategoryDto;
import ru.practicum.explorewithme.events.dto.annotations.ValidatedStateEnum;
import ru.practicum.explorewithme.users.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto {
    private Long id;
    @NotNull
    @Size(min = 1)
    private String annotation;
    @NotNull
    private CategoryDto category;
    private Integer confirmedRequests;
    @PastOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
    private String description;
    @NotNull
    @FutureOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @NotNull
    private UserShortDto initiator;
    @NotNull
    private LocationDto location;
    @NotNull
    private Boolean paid;
    private Integer participantLimit;
    @PastOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    @ValidatedStateEnum(acceptedValues = {"PENDING", "PUBLISHED", "CANCELED"})
    private String state;
    @NotNull
    @Size(min = 1)
    private String title;
    private Long views;
}