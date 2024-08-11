package ru.practicum.explorewithme.compilations.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.explorewithme.compilations.model.EventCompilation;

import java.util.List;

public interface EventCompilationRepository
        extends PagingAndSortingRepository<EventCompilation, Long>, CrudRepository<EventCompilation, Long> {
    @Query(value = "select event_id from compilations_events where compilation_id = ?1", nativeQuery = true)
    List<Long> findEventIdsByCompId(Long compId);

    void deleteByCompilationId(Long compilationId);
}
