package org.bri.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.bri.usercenter.model.User;
import org.bri.usercenter.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热的定时任务
 */
@Component
public class PreCacheJob {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserService userService;

    private List<Long> coreUsersId = List.of(5L, 6L);

    @Scheduled(cron = "0 0 0 * * *")
    public void preCache() {
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
}
