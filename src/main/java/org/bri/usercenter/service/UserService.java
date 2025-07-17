package org.bri.usercenter.service;

import jakarta.servlet.http.HttpServletRequest;
import org.bri.usercenter.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author ThinkPad
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-07-02 22:57:06
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long register(String userAccount, String userPassword, String checkPassword);

    /**
     * @param userAccount
     * @param userPassword
     * @return 返回脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注销，登出
     *
     * @param request
     */
    void userLogout(HttpServletRequest request);

    /**
     * 用户数据脱敏
     *
     * @param originUser
     * @return 脱敏处理后的用户数据
     */
    public User getSafeUser(User originUser);


    /**
     * 使用sql来根据tag搜素用户
     * @param tagList
     * @return
     */
    List<User> searchUserByTagsUsingSql(List<String> tagList);

    /**
     * 直接在内存里根据tag搜素用户
     * @param tagList
     * @return
     */
    List<User> searchUserByTagsUsingMemory(List<String> tagList);

}
