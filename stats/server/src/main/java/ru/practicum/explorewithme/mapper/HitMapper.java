package ru.practicum.explorewithme.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.HitDto;
import ru.practicum.explorewithme.model.Hit;

/**
 * Утилитарный класс содержит методы по преобразованию
 * объектов типа HitDto в Hit и обратно.
 */
@Component
public class HitMapper {
    public Hit toHit(HitDto hitDto) {
        if (hitDto == null)
            return null;

        return new Hit(
                hitDto.getId(),
                hitDto.getApp(),
                hitDto.getUri(),
                hitDto.getIp(),
                hitDto.getTimestamp());
    }


    public HitDto toHitDto(Hit hit) {
        if (hit == null)
            return null;

        return new HitDto(
                hit.getId(),
                hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                hit.getTimestamp());
    }
}
