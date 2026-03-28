package kr.ac.cbnu.tux.domain.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
@Service
public class ViewCountService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * @param domain     "community" 또는 "referenceroom"
     * @param id         게시글 ID
     * @param identifier 로그인 사용자: userId.toString() / 비로그인: IP 주소
     */
    public void addView(String domain, Long id, String identifier) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String viewSetKey = "view:" + domain + ":" + id + ":" + date;
        String viewCountKey = "viewCount:" + domain + ":" + id + ":" + date;

        try {
            Long added = stringRedisTemplate.opsForSet().add(viewSetKey, identifier);
            if (added != null && added == 1) {
                stringRedisTemplate.opsForValue().increment(viewCountKey);
                // TTL이 없는 경우에만 설정 (-1: TTL 없음)
                Long ttl = stringRedisTemplate.getExpire(viewSetKey);
                if (ttl != null && ttl == -1L) {
                    stringRedisTemplate.expire(viewSetKey, Duration.ofHours(25));
                }
            }
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed - view count not recorded for {}:{}", domain, id, e);
        }
    }
}
