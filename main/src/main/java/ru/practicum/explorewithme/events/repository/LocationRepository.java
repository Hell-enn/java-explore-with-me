package ru.practicum.explorewithme.events.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.explorewithme.events.model.Location;

import java.util.List;

public interface LocationRepository extends PagingAndSortingRepository<Location, Long>, CrudRepository<Location, Long> {
    List<Location> findByLatAndLon(Double lat, Double lon);
}
