package org.bri.usercenter.service;

import jakarta.annotation.Resource;
import org.bri.usercenter.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceTest {
    @Resource
    private UserService userService;

    @Test
    @Disabled
    void testAddUser() {
        User user = new User();
        user.setUsername("Test");
        user.setUserAccount("123");
        user.setAvatarUrl("https://liaoxuefeng.com/static/logo.svg");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123456");
        user.setEmail("343434mail");
        user.setUserStatus(0);

        boolean res = userService.save(user);
        System.out.println(userService.getById(1));
        Assertions.assertTrue(res);
    }

    @Test
    @Disabled
    void register() {
        String userAccount = "brioi";
        String password = "";
        String checkPassword = "123";
        long id = userService.register(userAccount, password, checkPassword);
        Assertions.assertEquals(id,-1);

        userAccount = "bir";
        id = userService.register(userAccount, password, checkPassword);
        Assertions.assertEquals(id,-1);

        userAccount = "Testyupi";
        id = userService.register(userAccount, password, checkPassword);
        Assertions.assertEquals(id,-1);

        userAccount = "brio";
        password = "1234abcd";
        checkPassword = "1234abcd";
        id = userService.register(userAccount, password, checkPassword);
        Assertions.assertEquals(id,-1);

        userAccount = "brioi";
        password = "1234abcd";
        checkPassword = "1234abcd";
        id = userService.register(userAccount, password, checkPassword);
        Assertions.assertTrue(id > 0);
    }
}