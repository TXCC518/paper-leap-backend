package com.may.paperleap.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.may.paperleap.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 28789
* @description 针对表【user】的数据库操作Service
* @createDate 2025-03-31 23:43:46
*/
public interface UserService extends IService<User> {
    long userRegister(String userAccount, String userPassword, String checkPassword);

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    List<User> searchUsers(String username, HttpServletRequest request);

    boolean deleteUser(Integer id, HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);

    User getSafetyUser(User user);

    List<User> searchUserByTagListByCPU(String tags);

    IPage<User> recommend(int page, int size, HttpServletRequest request);

    boolean updateUser(User user, HttpServletRequest request);
}
