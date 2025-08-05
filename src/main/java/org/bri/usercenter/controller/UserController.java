package org.bri.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.bri.usercenter.common.BaseResponse;
import org.bri.usercenter.common.ErrorCode;
import org.bri.usercenter.common.BusinessException;
import org.bri.usercenter.model.domain.User;
import org.bri.usercenter.model.request.UserLoginRequest;
import org.bri.usercenter.model.request.UserRegisterRequest;
import org.bri.usercenter.model.vo.UserVO;
import org.bri.usercenter.service.UserService;
import org.bri.usercenter.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.NumberUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

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

    /**
     * 推荐最相似的用户
     *
     * @param num 推荐数量
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> getMatchUsers(@RequestParam Integer num, HttpServletRequest request) {
        if(num == null || num <= 0 || num > 10) {
            return ResponseUtils.error(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.getMatchUsers(num, request);
        return ResponseUtils.success(userList);
    }

    /**
     * 获取推荐用户，根据分页返回
     * @param pageSize 每页显示多少记录
     * @param curPage 查询第几页
     * @param request 用户请求体
     * @return 返回分页后的用户列表
     */
    @GetMapping("/recommend")
    public BaseResponse<List<User>> getRecommendations(int pageSize,int curPage, HttpServletRequest request) {
        // 先尝试读缓存
        User loginUser = userService.getCurLoginUser(request);
        String redisKey = String.format("pattern:user:recommend:%s", loginUser.getId());
        Object cachedUserList = redisTemplate.opsForValue().get(redisKey);
        // redis中有缓存
        if (cachedUserList != null) {
            return ResponseUtils.success((List<User>) cachedUserList);
        }
        // 无缓存
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        IPage<User> page = new Page<>(curPage, pageSize);
        IPage<User> userPage = userService.page(page, queryWrapper); // 调用 page 方法
        List<User> userList = userPage.getRecords();
        List<User> safeUserList = userList.stream().map(user -> userService.getSafeUser(user)).toList();
        // 写入缓存
        redisTemplate.opsForValue().set(redisKey, safeUserList, 1, TimeUnit.MINUTES);
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
