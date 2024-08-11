package ru.practicum.explorewithme.events.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.explorewithme.events.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends PagingAndSortingRepository<Event, Long>, CrudRepository<Event, Long> {
    @Query(value = "select e.* " +
            "from events e " +
            "join compilations_events ce on e.event_id = ce.event_id " +
            "join compilations c on ce.compilation_id = c.compilation_id " +
            "where c.compilation_id = ?1 " +
            "order by c.compilation_id", nativeQuery = true)
    List<Event> findEventsByCompilation(Long compId);

    @Query(value = "select e.event_id " +
            "from events e " +
            "join compilations_events ce on e.event_id = ce.event_id " +
            "where ce.compilation_id = ?1", nativeQuery = true)
    List<Long> findCompilationEventIds(Long compId);

    @Query(value = "select count(*) from events where user_id = ?1", nativeQuery = true)
    int findUserEventsAmount(Long userId);


    @Query("select e from Event e " +
            "join e.user as u " +
            "where u.id = ?1 " +
            "order by e.eventDate")
    List<Event> findByUserIdOrderByEventDate(Long userId, Pageable page);

    @Query(value = "select * from events where event_id = ?1 and user_id = ?2", nativeQuery = true)
    Event findByEventIdAndUserId(Long eventId, Long userId);

    @Query(value = "select count(*) from events", nativeQuery = true)
    int findEventsAmount();

    @Query(value = "select * " +
            "from events " +
            "where user_id in ?1 and state in ?2 " +
            "and category_id in ?3 and event_date between ?4 and ?5 " +
            "order by event_id", nativeQuery = true)
    List<Event> findAdminEvents(List<Long> users,
                           List<String> states,
                           List<Long> categories,
                           LocalDateTime rangeStart,
                           LocalDateTime rangeEnd,
                           Pageable page);

    @Query(value = "select count(*) " +
                    "from events " +
                    "where user_id in ?1 and state in ?2 " +
                    "and category_id in ?3 and event_date between ?4 and ?5", nativeQuery = true)
    int findAdminEventsAmount(List<Long> users,
                                List<String> states,
                                List<Long> categories,
                                LocalDateTime rangeStart,
                                LocalDateTime rangeEnd);

    @Query(value = "select * " +
                    "from events " +
                    "where ((lower(annotation) like lower(concat('%',?1,'%'))) or (lower(description) like lower(concat('%',?2,'%')))) " +
                    "and category_id in ?3 and paid in ?4 " +
                    "and event_date between ?5 and ?6 and state = 'PUBLISHED' " +
                    "order by event_date", nativeQuery = true)
    List<Event> findPublicEventsWithSortingByEventDate(String textForAnnotation,
                                                        String textForDescription,
                                                        List<Long> categories,
                                                        List<Boolean> paid,
                                                        LocalDateTime rangeStart,
                                                        LocalDateTime rangeEnd,
                                                        Pageable page);

    @Query(value = "select * " +
            "from events " +
            "where ((lower(annotation) like lower(concat('%',?1,'%'))) or (lower(description) like lower(concat('%',?2,'%')))) " +
            "and category_id in ?3 and paid in ?4 " +
            "and event_date between ?5 and ?6 and state = 'PUBLISHED' " +
            "order by event_id", nativeQuery = true)
    List<Event> findPublicEventsWithSortingByEventId(String textForAnnotation,
                                                       String textForDescription,
                                                       List<Long> categories,
                                                       List<Boolean> paid,
                                                       LocalDateTime rangeStart,
                                                       LocalDateTime rangeEnd,
                                                       Pageable page);

    @Query(value = "select * " +
            "from events " +
            "where ((lower(annotation) like lower(concat('%',?1,'%'))) or (lower(description) like lower(concat('%',?2,'%')))) " +
            "and category_id in ?3 and paid in ?4 " +
            "and event_date between ?5 and ?6 and e.state = 'PUBLISHED' ", nativeQuery = true)
    List<Event> findPublicEvents(String textForAnnotation,
                                                     String textForDescription,
                                                     List<Long> categories,
                                                     List<Boolean> paid,
                                                     LocalDateTime rangeStart,
                                                     LocalDateTime rangeEnd,
                                                     Pageable page);

    @Query(value =  "select count(*) " +
                    "from events " +
                    "where ((lower(annotation) like lower(concat('%',?1,'%'))) or (lower(description) like lower(concat('%',?2,'%')))) " +
                    "and category_id in ?3 and paid in ?4 " +
                    "and event_date between ?5 and ?6 and state ilike 'PUBLISHED' ", nativeQuery = true)
    int findPublicEventsAmount(String textForAnnotation,
                                         String textForDescription,
                                         List<Long> categories,
                                         List<Boolean> paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd);

    @Query(value = "select * from events where event_id = ?1 and state ilike ?2", nativeQuery = true)
    Optional<Event> findPublicEvent(Long eventId, String state);
}
