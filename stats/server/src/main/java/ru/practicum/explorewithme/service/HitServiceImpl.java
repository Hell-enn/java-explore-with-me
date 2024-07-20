package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.EndpointStatisticsDto;
import ru.practicum.explorewithme.HitDto;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.mapper.HitMapper;
import ru.practicum.explorewithme.repository.HitJpaRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * Класс HitServiceImpl предоставляет функциональность по
 * взаимодействию с объектами с информацией о запросах -
 * объекты типа Hit
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HitServiceImpl implements HitService {

    private final HitJpaRepository hitJpaRepository;
    private final HitMapper hitMapper;

    @Override
    @Transactional
    public HitDto postHit(HitDto hitDto) {
        log.debug("Сохранение информации о запросе о мероприятии c эндпоинтом {}", hitDto.getUri());
        return hitMapper.toHitDto(hitJpaRepository.save(hitMapper.toHit(hitDto)));
    }


    @Override
    @Transactional(readOnly = true)
    public List<EndpointStatisticsDto> getStatistics(String start, String end, String[] uris, Boolean unique) {

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endTime = LocalDateTime.parse(end, formatter);

        if (startTime.isAfter(endTime) || startTime.equals(endTime))
            throw new BadRequestException("Дата конца не должна предшествовать лате начала!");

        List<EndpointStatisticsDto> statistics;

        if (uris != null && uris.length > 0) {
            statistics = unique ? hitJpaRepository.findUniqueRequestsAmountWithUris(startTime, endTime, uris)
                    : hitJpaRepository.findNotUniqueRequestsAmountWithUris(startTime, endTime, uris);
        } else {
            statistics =  unique ? hitJpaRepository.findUniqueRequestsAmountWithoutUris(startTime, endTime)
                    : hitJpaRepository.findNotUniqueRequestsAmountWithoutUris(startTime, endTime);
        }

        log.debug("Получена статистика с {} до {} по эндпоинтам {}", start, end, uris);
        return statistics;
    }
}
