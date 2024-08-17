package ru.practicum.explorewithme.compilations.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.explorewithme.compilations.model.Compilation;

import java.util.List;

public interface CompilationRepository extends PagingAndSortingRepository<Compilation, Long>, CrudRepository<Compilation, Long> {

    @Query(value = "select * from compilations where pinned IN ?1 order by compilation_id", nativeQuery = true)
    List<Compilation> findByPinnedOrderById(List<Boolean> pinnedList, Pageable page);

    @Query(value = "SELECT count(*) FROM compilations WHERE pinned IN ?1", nativeQuery = true)
    int findPinnedAmount(List<Boolean> pinnedList);
}
