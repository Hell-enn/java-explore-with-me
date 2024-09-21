package ru.practicum.explorewithme.subscription.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.explorewithme.subscription.model.Subscription;

import java.util.Optional;

public interface SubscriptionRepository extends PagingAndSortingRepository<Subscription, Long>, CrudRepository<Subscription, Long> {
    @Query(value = "select * " +
                    "from subscriptions " +
                    "where follower_id = ?1 and followed_id = ?2",
            nativeQuery = true)
    Optional<Subscription> findByFollowerAndFollowed(Long followerId, Long followedId);
}
