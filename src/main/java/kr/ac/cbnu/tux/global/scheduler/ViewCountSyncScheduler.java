package kr.ac.cbnu.tux.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ViewCountSyncScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 */10 * * * *")
    public void syncViewCounts() {
        syncDomain("community", "community");
        syncDomain("referenceroom", "reference_room");
    }

    private void syncDomain(String keyDomain, String tableName) {
        String pattern = "viewCount:" + keyDomain + ":*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

        List<String> keys = new ArrayList<>();
        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            cursor.forEachRemaining(keys::add);
        }

        if (keys.isEmpty()) return;

        List<Object[]> batchArgs = new ArrayList<>();
        for (String key : keys) {
            // GETSET: 현재 값을 원자적으로 가져오고 0으로 초기화
            String value = stringRedisTemplate.opsForValue().getAndSet(key, "0");
            if (value == null || value.equals("0")) continue;

            // key 형식: viewCount:{domain}:{id}:{date}
            String[] parts = key.split(":");
            if (parts.length < 3) continue;

            try {
                Long postId = Long.parseLong(parts[2]);
                long count = Long.parseLong(value);
                batchArgs.add(new Object[]{count, postId});
            } catch (NumberFormatException e) {
                log.warn("조회수 동기화 키 파싱 실패: {}", key);
            }
        }

        if (batchArgs.isEmpty()) return;

        jdbcTemplate.batchUpdate(
                "UPDATE " + tableName + " SET view = view + ? WHERE id = ?",
                batchArgs
        );

        log.debug("조회수 동기화 완료: domain={}, count={}건", keyDomain, batchArgs.size());
    }
}
