package org.bri.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bri.usercenter.common.ErrorCode;
import org.bri.usercenter.common.BusinessException;
import org.bri.usercenter.model.User;
import org.bri.usercenter.service.UserService;
import org.bri.usercenter.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bri.usercenter.constant.UserConstant.*;

/**
 * @author ThinkPad
 * @description 针对表【user(用户)】的数据库操作Serviced的实现
 * @createDate 2025-07-02 22:57:06
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 混淆
     */
    private static final String salt = "bri";

    @Override
    public long register(String userAccount, String userPassword, String checkPassword) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //账户不能包含特殊字符
        String validPatter = "[`~!#$%^&*()+=|{}'Aa:;',\\\\[\\\\].<>/?~！@#￥%……&*（）9——+|{}【】\\\"‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPatter).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户包含特殊字符");
        }
        //密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不一致");
        }
        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"已存在账户");
        }
        //2.加密
        String encodedPassword = DigestUtils.md5DigestAsHex((salt + userPassword).getBytes(StandardCharsets.UTF_8));
        //3.插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encodedPassword);
        boolean saved = this.save(user);
        if (!saved) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return user.getId();
    }


    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //账户不能包含特殊字符
        String validPatter = "[`~!#$%^&*()+=|{}'Aa:;',\\\\[\\\\].<>/?~！@#￥%……&*（）9——+|{}【】\\\"‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPatter).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.加密
        String encodedPassword = DigestUtils.md5DigestAsHex((salt + userPassword).getBytes(StandardCharsets.UTF_8));
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encodedPassword);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
//            log.info("user login failed: account or password is incorrect");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }
        //3.数据脱敏
        var safeUser = getSafeUser(user);
        //4.记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return safeUser;
    }

    @Override
    public void userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
    }

    @Override
    public User getSafeUser(User originUser) {
        if(originUser == null) {
            return null;
        }
        User safeUser = new User();
        safeUser.setId(originUser.getId());
        safeUser.setUsername(originUser.getUsername());
        safeUser.setUserAccount(originUser.getUserAccount());
        safeUser.setAvatarUrl(originUser.getAvatarUrl());
        safeUser.setGender(originUser.getGender());
//        safeUser.setUserPassword("");
        safeUser.setPhone(originUser.getPhone());
        safeUser.setEmail(originUser.getEmail());
        safeUser.setUserRole(originUser.getUserRole());
        safeUser.setUserStatus(originUser.getUserStatus());
        safeUser.setCreateTime(originUser.getCreateTime());
//        safeUser.setUpdateTime(new Date());
//        safeUser.setIsDelete(0);
        return safeUser;
    }
}




