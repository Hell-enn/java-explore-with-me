package ru.practicum.explorewithme.compilations.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.explorewithme.events.model.Event;

import javax.persistence.*;

@Entity
@Table(name = "compilations_events", schema = "public")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventCompilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compilation_event_id", insertable = false, updatable = false)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "compilation_id")
    private Compilation compilation;
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
}
