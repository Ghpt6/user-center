package org.bri.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.bri.usercenter.model.User;
import org.bri.usercenter.service.UserService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热的定时任务
 */
@Slf4j
@Component
public class PreCacheJob {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private RedissonClient redisson;

    private List<Long> coreUsersId = List.of(5L, 6L);

    @Scheduled(cron = "0 0 0 * * *")
    public void preCache() {
        RLock lock = redisson.getLock("pattern:precache:lock");
        try {
            // 尝试获取锁
            if (lock.tryLock(0, TimeUnit.SECONDS)) {
                // 不指定释放时间会指定ttl为30s，并且会每10s自动续约
                log.info("getlock by {}", Thread.currentThread().getName());
                for (long userId : coreUsersId) {
                    String redisKey = String.format("pattern:user:recommend:%s", userId);

                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    IPage<User> page = new Page<>(1, 10);
                    IPage<User> userPage = userService.page(page, queryWrapper); // 调用 page 方法
                    List<User> userList = userPage.getRecords();
                    List<User> safeUserList = userList.stream().map(u -> userService.getSafeUser(u)).toList();
                    redisTemplate.opsForValue().set(redisKey, safeUserList, 1, TimeUnit.MINUTES);
                }
            }
        } catch (InterruptedException e) {
            log.error("precache error: {}", e.getMessage());
        } finally {
            // 释放锁，判断是否是自己加的锁
            if (lock.isHeldByCurrentThread()) {
                log.info("unlock by {}", Thread.currentThread().getName());
                lock.unlock();
            }
        }
    }
}
