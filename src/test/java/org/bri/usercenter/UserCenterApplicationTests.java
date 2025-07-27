package org.bri.usercenter;


import jakarta.annotation.Resource;
import org.bri.usercenter.mapper.UserMapper;
import org.bri.usercenter.model.domain.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserCenterApplicationTests {
    @Resource
    private UserMapper userMapper;


    @Test
    @Disabled
    public void test() {
        System.out.println("--test--");
        List<User> users = userMapper.selectList(null);
        users.forEach(System.out::println);
    }
}