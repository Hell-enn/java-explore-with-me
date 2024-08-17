package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.EndpointStatisticsDto;
import ru.practicum.explorewithme.HitDto;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.mapper.HitMapper;
import ru.practicum.explorewithme.model.Hit;
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
        Hit hit = hitMapper.toHit(hitDto);
        Hit savedHit = hitJpaRepository.save(hit);
        return hitMapper.toHitDto(savedHit);
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

        List<String> correctedUris = new ArrayList<>();
        uris.forEach(uri -> {
            if (!uri.equals("[]")) {
                if (uri.startsWith("["))
                    uri = uri.substring(1);
                if (uri.endsWith("]"))
                    uri = uri.substring(0, uri.length() - 1);
                correctedUris.add(uri);
            }
        });

        if (!correctedUris.isEmpty()) {
            statistics = unique ? hitJpaRepository.findUniqueRequestsAmountWithUris(startTime, endTime, correctedUris)
                    : hitJpaRepository.findNotUniqueRequestsAmountWithUris(startTime, endTime, correctedUris);
        } else {
            statistics =  unique ? hitJpaRepository.findUniqueRequestsAmountWithoutUris(startTime, endTime)
                    : hitJpaRepository.findNotUniqueRequestsAmountWithoutUris(startTime, endTime);
        }

        Map<String, EndpointStatisticsDto> statsToCheck = new HashMap<>();
        statistics.forEach(stat -> statsToCheck.put(stat.getUri(), stat));
        correctedUris.forEach(correctedUri -> {
            if (!correctedUri.equals("[]")) {
                statsToCheck.put(correctedUri, statsToCheck.getOrDefault(correctedUri, new EndpointStatisticsDto("ewm-main-service", correctedUri, 0L)));
            }
        });

        log.debug("Получена статистика с {} до {} по эндпоинтам {}", start, end, correctedUris);
        return new ArrayList<>(statsToCheck.values());
    }
}
