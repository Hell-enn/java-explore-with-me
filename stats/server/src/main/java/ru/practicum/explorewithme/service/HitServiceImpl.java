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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    public List<EndpointStatisticsDto> getStatistics(String start, String end, List<String> uris, Boolean unique) {

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endTime = LocalDateTime.parse(end, formatter);

        if (startTime.isAfter(endTime))
            throw new BadRequestException("Дата конца не должна предшествовать дате начала!");

        List<EndpointStatisticsDto> statistics;

        if (uris != null && !uris.isEmpty()) {
            statistics = unique ? hitJpaRepository.findUniqueRequestsAmountWithUris(startTime, endTime, uris)
                    : hitJpaRepository.findNotUniqueRequestsAmountWithUris(startTime, endTime, uris);
        } else {
            statistics =  unique ? hitJpaRepository.findUniqueRequestsAmountWithoutUris(startTime, endTime)
                    : hitJpaRepository.findNotUniqueRequestsAmountWithoutUris(startTime, endTime);
        }

        Map<String, EndpointStatisticsDto> statsToCheck = new HashMap<>();
        if (uris != null && statistics.size() < uris.size()) {
            statistics.forEach(stat -> statsToCheck.put(stat.getUri(), stat));
            uris.forEach(uri -> {
                statsToCheck.put(uri, statsToCheck.getOrDefault(uri, new EndpointStatisticsDto("ewm-main-service", uri, 0L)));
            });
        }

        log.debug("Получена статистика с {} до {} по эндпоинтам {}", start, end, uris);
        return new ArrayList<>(statsToCheck.values());
    }
}
