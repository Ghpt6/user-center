package org.bri.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.bri.usercenter.common.BaseResponse;
import org.bri.usercenter.common.ErrorCode;
import org.bri.usercenter.common.BusinessException;
import org.bri.usercenter.model.User;
import org.bri.usercenter.model.request.UserLoginRequest;
import org.bri.usercenter.model.request.UserRegisterRequest;
import org.bri.usercenter.service.UserService;
import org.bri.usercenter.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.bri.usercenter.constant.UserConstant.ADMIN_ROLE;
import static org.bri.usercenter.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest req) {
        if (req == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userId = userService.register(req.getUserAccount(), req.getUserPassword(), req.getCheckPassword());
        return ResponseUtils.success(userId);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest loginRequest, HttpServletRequest request) {
        if (loginRequest == null) {
            return ResponseUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(loginRequest.getUserAccount(), loginRequest.getUserPassword(), request);
        return ResponseUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Void> userLogout(HttpServletRequest request) {
        if (request != null) {
            userService.userLogout(request);
        }
        return ResponseUtils.success(null);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj instanceof User currentUser) {
            User userId = userService.getById(currentUser.getId());
            User safeUser = userService.getSafeUser(userId);
            return ResponseUtils.success(safeUser);
        }
        return null;
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUser(String username, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return null;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //todo
//        queryWrapper.like("username", username);
        List<User> userList = userService.list(queryWrapper);
        List<User> collect = userList.stream().map(user -> userService.getSafeUser(user)).collect(Collectors.toList());
        return ResponseUtils.success(collect);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (id <= 0) {
            return ResponseUtils.error(ErrorCode.PARAMS_ERROR);
        }
        if (!isAdmin(request)) {
            return ResponseUtils.error(ErrorCode.NO_AUTH_ERROR);
        }
        Boolean removed = userService.removeById(id);
        return ResponseUtils.success(removed);
    }

    private boolean isAdmin(HttpServletRequest request) {
        //权限管理，仅管理员
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj instanceof User user) {
            return user.getUserRole() == ADMIN_ROLE;
        }
        return false;
    }
}
