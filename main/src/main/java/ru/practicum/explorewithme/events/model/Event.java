package ru.practicum.explorewithme.events.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.explorewithme.categories.model.Category;
import ru.practicum.explorewithme.events.dto.enums.State;
import ru.practicum.explorewithme.users.model.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events", schema = "public")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", insertable = false, updatable = false)
    private Long id;
    private String annotation;
    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;
    private String description;
    @Column(name = "event_date")
    private LocalDateTime eventDate;
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    @Column(name = "request_moderation")
    private Boolean requestModeration;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private String title;
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private State state;
}
