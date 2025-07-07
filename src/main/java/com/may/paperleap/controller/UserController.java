package com.may.paperleap.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.may.paperleap.common.BaseResponse;
import com.may.paperleap.common.CodeError;
import com.may.paperleap.common.ResultUtils;
import com.may.paperleap.constant.UserConstant;
import com.may.paperleap.exception.BusinessException;
import com.may.paperleap.model.domain.User;
import com.may.paperleap.model.request.UserLoginRequest;
import com.may.paperleap.model.request.UserRegisterRequest;
import com.may.paperleap.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户接口
 *
 * @author May20242
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户相关接口")  // SpringDoc 注解
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(CodeError.NULL_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(CodeError.NULL_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);

        return ResultUtils.success(CodeError.SUCCESS, result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(CodeError.NULL_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(CodeError.NULL_ERROR);
        }
        User result = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(CodeError.SUCCESS, result);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        List<User> result = userService.searchUsers(username, request);

        return ResultUtils.success(CodeError.SUCCESS, result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(Integer id, HttpServletRequest request) {
        boolean result = userService.deleteUser(id, request);

        return ResultUtils.success(CodeError.SUCCESS, result);
    }

    @GetMapping("/search/user")
    public BaseResponse<List<User>> searchUsersByTags(String tags) {
        List<User> users = userService.searchUserByTagListByCPU(tags);

        return ResultUtils.success(CodeError.SUCCESS, users);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(CodeError.NOT_LOGIN);
        }
        Long userId = currentUser.getId();
        // TODO 检验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);

        return ResultUtils.success(CodeError.SUCCESS, safetyUser);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            return ResultUtils.error(CodeError.PARAMS_ERROR);
        }
        boolean res = userService.updateUser(user, request);

        return ResultUtils.success(CodeError.SUCCESS, res);
    }

    @GetMapping("/recommend")
    public BaseResponse<IPage<User>> getRecommendUsers(int page, int size, HttpServletRequest request) {
        IPage<User> recommend = userService.recommend(page, size, request);
        return ResultUtils.success(CodeError.SUCCESS, recommend);
    }

}
