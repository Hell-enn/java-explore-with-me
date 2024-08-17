package ru.practicum.explorewithme.users.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.explorewithme.users.model.User;

import java.util.List;

public interface UserRepository extends PagingAndSortingRepository<User, Long>, CrudRepository<User, Long> {
    @Query(value = "select * from users where email = ?1", nativeQuery = true)
    List<User> findByEmail(String email);

    @Query(value = "select count(*) from users", nativeQuery = true)
    int findAmount();

    @Query(value = "select * from users where user_id in ?1 order by user_id", nativeQuery = true)
    List<User> findAllByIds(List<Long> ids, Pageable page);

    @Query(value = "select * from users order by user_id", nativeQuery = true)
    List<User> findAllUsers(Pageable page);

    @Query(value = "select user_id from users", nativeQuery = true)
    List<Long> findUserIds();
}
