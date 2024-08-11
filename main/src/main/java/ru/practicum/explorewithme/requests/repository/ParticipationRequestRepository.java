package ru.practicum.explorewithme.requests.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.explorewithme.requests.model.ParticipationRequest;

import java.util.List;

public interface ParticipationRequestRepository
        extends PagingAndSortingRepository<ParticipationRequest, Long>, CrudRepository<ParticipationRequest, Long> {
    @Query(value = "select count(*) " +
            "from requests " +
            "where event_id = ?1 and status ilike ?2",
            nativeQuery = true)
    int findRequestAmount(Long eventId, String status);

    List<ParticipationRequest> findByEventIdAndUserId(Long eventId, Long userId);

    @Query(value = "select * " +
            "from requests " +
            "where event_id = ?1 and request_id in ?2 " +
            "order by request_id", nativeQuery = true)
    List<ParticipationRequest> findByEventIdAndRequestIdOrderByRequestId(Long eventId, List<Long> requestId);

    @Query(value = "select count(*) " +
            "from requests " +
            "where event_id = ?1 and user_id = ?2 and status = ?3",
            nativeQuery = true)
    int findByEventIdAndUserIdAndStatusAmount(Long eventId, Long userId, String status);

    @Query(value = "select count(*) " +
            "from requests " +
            "where event_id = ?1 and status = ?2",
            nativeQuery = true)
    int findByEventIdAndStatusAmount(Long eventId, String status);

    List<ParticipationRequest> findByUserId(Long userId);

    List<ParticipationRequest> findByEventId(Long eventId);
}