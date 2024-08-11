package ru.practicum.explorewithme.categories.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.explorewithme.categories.model.Category;

import java.util.List;

public interface CategoryRepository extends PagingAndSortingRepository<Category, Long>, CrudRepository<Category, Long> {
    Category findByName(String name);

    @Query(value = "SELECT count(c.*) FROM categories AS c", nativeQuery = true)
    int findAmount();

    @Query(value = "select category_id from categories", nativeQuery = true)
    List<Long> findCategoryIds();

    @Query(value = "select count(*) from events where category_id = ?1", nativeQuery = true)
    int findEventsAmountByCategory(Long catId);
}
