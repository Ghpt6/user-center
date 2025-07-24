package org.bri.usercenter.config;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedissonConfigTest {
    @Autowired
    RedissonClient redisson;

    @Test
    void testRedissonConfig() {
        // list
        RList<String> list = redisson.getList("list-test");
        list.add("brio");
        String s = list.get(0);
        assertEquals("brio", s);
        int size = list.size();
        System.out.println(size);
    }
}