package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.explorewithme.EndpointStatisticsDto;
import ru.practicum.explorewithme.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitJpaRepository  extends PagingAndSortingRepository<Hit, Long>, CrudRepository<Hit, Long> {
    @Query(name =  "find_unique_stats_with_uris", nativeQuery = true)
    List<EndpointStatisticsDto> findUniqueRequestsAmountWithUris(LocalDateTime start, LocalDateTime end, List<String> usris);

    @Query(name =  "find_stats_with_uris", nativeQuery = true)
    List<EndpointStatisticsDto> findNotUniqueRequestsAmountWithUris(LocalDateTime start, LocalDateTime end, List<String> usris);

    @Query(name =  "find_unique_stats", nativeQuery = true)
    List<EndpointStatisticsDto> findUniqueRequestsAmountWithoutUris(LocalDateTime start, LocalDateTime end);

    @Query(name =  "find_stats", nativeQuery = true)
    List<EndpointStatisticsDto> findNotUniqueRequestsAmountWithoutUris(LocalDateTime start, LocalDateTime end);
}
