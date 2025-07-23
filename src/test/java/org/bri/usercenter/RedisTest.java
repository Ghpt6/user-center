package org.bri.usercenter;

import org.bri.usercenter.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("yupiString", "dog");
        valueOperations.set("yupiInt", 1);
        valueOperations.set("yupiDouble", 1.22);
        valueOperations.set("yupiUser", new User());

        Object yupiString = valueOperations.get("yupiString");
        Assertions.assertEquals("dog", yupiString);
        Object yupiInt = valueOperations.get("yupiInt");
        Assertions.assertEquals(1, yupiInt);
        Object yupiDouble = valueOperations.get("yupiDouble");
        Assertions.assertEquals(1.22, yupiDouble);
        Object yupiUser = valueOperations.get("yupiUser");
        Assertions.assertEquals(new User(), yupiUser);
    }
}
