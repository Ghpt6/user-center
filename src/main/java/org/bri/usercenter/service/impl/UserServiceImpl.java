package org.bri.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bri.usercenter.common.ErrorCode;
import org.bri.usercenter.common.BusinessException;
import org.bri.usercenter.model.domain.User;
import org.bri.usercenter.model.vo.UserVO;
import org.bri.usercenter.service.UserService;
import org.bri.usercenter.mapper.UserMapper;
import org.bri.usercenter.utils.EditDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bri.usercenter.constant.UserConstant.*;

/**
 * @author ThinkPad
 * &#064;description  针对表【user(用户)】的数据库操作Serviced的实现
 * &#064;createDate  2025-07-02 22:57:06
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已存在账户");
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
        if (originUser == null) {
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
        safeUser.setTags(originUser.getTags());
        return safeUser;
    }

    @Override
    public Integer updateUser(User user, HttpServletRequest request) {
        // 只有管理员和用户自己能更改
        if (!isAdmin(request) && getCurLoginUser(request).getId() != user.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        User originalUser = userMapper.selectById(user.getId());
        if (originalUser == null) {
            throw new BusinessException(ErrorCode.NO_USER_ERROR);
        }
        return userMapper.updateById(user);
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagList 用户要拥有的标签
     * @return
     */
    @Override
    public List<User> searchUserByTagsUsingSql(List<String> tagList) {
        if (CollectionUtils.isEmpty(tagList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "tag为空");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tag : tagList) {
            queryWrapper = queryWrapper.like("tags", tag);
        }
        List<User> users = userMapper.selectList(queryWrapper);
        // 2\
        return users.stream().map(this::getSafeUser).toList();
    }

    @Override
    public List<User> searchUserByTagsUsingMemory(List<String> tagList) {
        if (CollectionUtils.isEmpty(tagList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "tag为空");
        }
        // 查询所有
        List<User> users = userMapper.selectList(null);

        return users.stream().filter(user -> {
            String tags = user.getTags();
            Gson gson = new Gson();
            Set<String> jsonTags = gson.fromJson(tags, new TypeToken<Set<String>>() {
            }.getType());
            jsonTags = Optional.ofNullable(jsonTags).orElse(new HashSet<>());
            if (CollectionUtils.isEmpty(jsonTags)) {
                return false;
            }
            for (String tag : tagList) {
                if (!jsonTags.contains(tag)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafeUser).toList();
    }

    @Override
    public User getCurLoginUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN_ERROR);
        }
        Object currentUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser instanceof User cur) {
            long userId = cur.getId();
            User user = userMapper.selectById(userId);
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            return user;
        }
        throw new BusinessException(ErrorCode.NO_LOGIN_ERROR);
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        //权限管理，仅管理员
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj instanceof User user) {
            return user.getUserRole() == ADMIN_ROLE;
        }
        return false;
    }

    @Override
    public List<User> getMatchUsers(Integer num, HttpServletRequest request) {
        User loginUser = getCurLoginUser(request);
        Gson gson = new Gson();
        List<String> loginUserTags = gson.fromJson(loginUser.getTags(), new TypeToken<List<String>>() {
        });

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        queryWrapper.select("id", "tags"); // 只查询id，tags字段 34s -> 7s
        List<User> userList = this.list(queryWrapper);// 全部数据

        // <userList的下标, distance>
        SortedMap<Integer, Integer> indexDistanceMap = new TreeMap<>();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            List<String> userTags = gson.fromJson(user.getTags(), new TypeToken<List<String>>() {
            });
            // 标签为空 or 为用户自己
            if (CollectionUtils.isEmpty(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }

            int minDistance = EditDistance.minDistance(loginUserTags, userTags);
            indexDistanceMap.put(i, minDistance); // 存放所有编辑距离
        }
        List<Integer> topIndex = indexDistanceMap.keySet().stream().limit(num).toList();
        List<User> list = topIndex.stream().map(index -> {
            return userList.get(index);
        }).toList();
        return list;
    }
}




