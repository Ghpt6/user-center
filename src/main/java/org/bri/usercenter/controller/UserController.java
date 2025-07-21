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
import org.springframework.util.CollectionUtils;
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
@CrossOrigin(originPatterns = {"http://localhost:*"})
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
        User loginUser = userService.getCurLoginUser(request);
        User safeUser = userService.getSafeUser(loginUser);
        return ResponseUtils.success(safeUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUser(@RequestParam String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            return ResponseUtils.error(ErrorCode.NO_AUTH_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //todo
//        queryWrapper.like("username", username);
        List<User> userList = userService.list(queryWrapper);
        List<User> collect = userList.stream().map(user -> userService.getSafeUser(user)).collect(Collectors.toList());
        return ResponseUtils.success(collect);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserTags(@RequestParam List<String> tagList, HttpServletRequest request) {
        if (CollectionUtils.isEmpty(tagList)) {
            return ResponseUtils.error(ErrorCode.PARAMS_ERROR);
        }
        List<User> users = userService.searchUserByTagsUsingMemory(tagList);
        return ResponseUtils.success(users);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            return ResponseUtils.error(ErrorCode.PARAMS_ERROR);
        }
        Integer id = userService.updateUser(user, request);
        return ResponseUtils.success(id);
    }

    @GetMapping("/recommend")
    public BaseResponse<List<User>> getRecommendations(HttpServletRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userService.list(queryWrapper);
        List<User> safeUserList = userList.stream().map(user -> userService.getSafeUser(user)).toList();
        return ResponseUtils.success(safeUserList);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (id <= 0) {
            return ResponseUtils.error(ErrorCode.PARAMS_ERROR);
        }
        if (!userService.isAdmin(request)) {
            return ResponseUtils.error(ErrorCode.NO_AUTH_ERROR);
        }
        Boolean removed = userService.removeById(id);
        return ResponseUtils.success(removed);
    }

}
