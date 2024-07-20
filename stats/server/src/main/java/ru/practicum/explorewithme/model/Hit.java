package ru.practicum.explorewithme.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.explorewithme.EndpointStatisticsDto;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Модель данных информации о вещи, используемая на уровне хранилища.
 */
@Entity
@Table(name = "hits", schema = "public")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@NamedNativeQuery(
        name = "find_unique_stats_with_uris",
        query =
                "SELECT app, url, count(distinct ip) as hits " +
                        "FROM hits " +
                        "WHERE moment BETWEEN ?1 AND ?2 AND url IN ?3 " +
                        "GROUP BY app, url",
        resultSetMapping = "endpoint_statistics_dto"
)
@NamedNativeQuery(
        name = "find_stats_with_uris",
        query =
                "SELECT app, url, count(ip) as hits " +
                        "FROM hits " +
                        "WHERE moment BETWEEN ?1 AND ?2 AND url IN ?3 " +
                        "GROUP BY app, url",
        resultSetMapping = "endpoint_statistics_dto"
)
@NamedNativeQuery(
        name = "find_unique_stats",
        query =
                "SELECT app, url, count(distinct ip) as hits " +
                        "FROM hits " +
                        "WHERE moment BETWEEN ?1 AND ?2 " +
                        "GROUP BY app, url",
        resultSetMapping = "endpoint_statistics_dto"
)
@NamedNativeQuery(
        name = "find_stats",
        query =
                "SELECT app, url, count(ip) as hits " +
                        "FROM hits " +
                        "WHERE moment BETWEEN ?1 AND ?2 " +
                        "GROUP BY app, url",
        resultSetMapping = "endpoint_statistics_dto"
)
@SqlResultSetMapping(
        name = "endpoint_statistics_dto",
        classes = @ConstructorResult(
                targetClass = EndpointStatisticsDto.class,
                columns = {
                        @ColumnResult(name = "app", type = String.class),
                        @ColumnResult(name = "url", type = String.class),
                        @ColumnResult(name = "hits", type = Long.class)
                }
        )
)
public class Hit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hit_id", insertable = false, updatable = false)
    private Long id;
    private String app;
    @Column(name = "url")
    private String uri;
    private String ip;
    @Column(name = "moment")
    private LocalDateTime timestamp;
}