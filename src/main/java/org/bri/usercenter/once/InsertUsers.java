package org.bri.usercenter.once;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import lombok.extern.slf4j.Slf4j;
import org.bri.usercenter.mapper.UserMapper;
import org.bri.usercenter.model.User;
import org.bri.usercenter.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Component
public class InsertUsers {
    @Autowired
    private UserService userService;

    /**
     * 批量插入用户
     * 插入10万条数据花费时间30-40秒
     */
    @Scheduled(initialDelay = 1000)
    public void insertUsers() {
        log.info("Inserting Users...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for (int i = 0; i < 20; i++) {
            List<User> users = createUsers(5000);
            userService.saveBatch(users);
        }
        stopWatch.stop();
        System.out.print("time cost: ");
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 并发的方法批量插入用户
     * 使用10个并发线程平均花费10秒左右
     */
//    @Scheduled(initialDelay = 1000)
    public void concurrentInsertUsers() {
        log.info("Inserting Users...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ExecutorService executorService = new ThreadPoolExecutor(10, 1000, 10L, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1000));
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<User> users = createUsers(10000);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> userService.saveBatch(users), executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();

        stopWatch.stop();
        System.out.print("time cost: ");
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    private List<User> createUsers(int insertNum) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < insertNum; i++) {
            User user = new User();
            user.setUsername("示例名称");
            user.setUserAccount("fakeaccount");
            user.setAvatarUrl("https://pica.zhimg.com/v2-d57f2e1ab5e644c143fcde1ca1038bd5_l.jpg?source=32738c0c&needBackground=1");
            user.setGender(1);
            user.setUserPassword("12345678");
            user.setPhone("123123");
            user.setEmail("132@132.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("[]");
            users.add(user);
        }
        return users;
    }
}
