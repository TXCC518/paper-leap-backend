package com.may.paperleap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.may.paperleap.constant.UserConstant;
import com.may.paperleap.mapper.UserMapper;
import com.may.paperleap.model.domain.User;
import com.may.paperleap.common.CodeError;
import com.may.paperleap.exception.BusinessException;
import com.may.paperleap.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
* @author 28789
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-03-31 23:43:46
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    /**
     * 盐值
     * 混淆密码
     */
    private final String SALT = "may";

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     *
     * @param userAccount   账号
     * @param userPassword  密码
     * @param checkPassword 确认密码
     * @return  用户id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 非空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(CodeError.NULL_ERROR);
        }
        // 账户长度不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        // 密码不小于8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        // 账户不包含特殊字符
        String regex = "[^a-zA-Z0-9_]";
        Matcher matcher = Pattern.compile(regex).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        Integer count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        // 对密码进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 将新用户插入数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        // 返回新用户的id
        return user.getId();
    }

    /**
     *
     * @param userAccount   账号
     * @param userPassword  密码
     * @param request       请求信息
     * @return
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 非空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(CodeError.NULL_ERROR);
        }
        // 账户长度不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        // 密码不小于8位
        if (userPassword.length() < 8) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        // 账户不包含特殊字符
        String regex = "[^a-zA-Z0-9_]";
        Matcher matcher = Pattern.compile(regex).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        // 密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        userQueryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(userQueryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        // 用户信息进行脱敏，隐藏敏感信息
        User safetyUser = getSafetyUser(user);
        // 记录用户的登录态，存储到session中
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, safetyUser);
        // 返回脱敏后的用户信息
        return safetyUser;
    }

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @param request  请求信息
     * @return 用户列表
     */
    @Override
    public List<User> searchUsers(String username, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(CodeError.NO_AUTH);
        }
        // 当前用户为管理员
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.eq("username", username);
        }
        return userMapper.selectList(queryWrapper).stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 根据用户id删除用户
     * @param id    用户id
     * @param request   请求信息
     * @return  是否删除用户
     */
    @Override
    public boolean deleteUser(Integer id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(CodeError.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(CodeError.PARAMS_ERROR);
        }
        int count = userMapper.deleteById(id);

        return count > 0;
    }

    /**
     * 判断当前用户是否是管理员
     * @param request 请求信息
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 获取登录态中的用户信息
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        // 当前用户为管理员
        if (user != null && user.getUserRole() == UserConstant.ADMIN_ROLE) {
            return true;
        }
        return false;
    }

    /**
     * 给用户信息进行脱敏
     * @param user  未脱敏的用户对象
     * @return
     */
    @Override
    public User getSafetyUser(User user) {
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setDescription(user.getDescription());
        safetyUser.setTags(user.getTags());
        return safetyUser;
    }

    /**
     * 得到当前登录用户对象
     * @param request   请求头
     * @return
     */
    public User getCurrentUser(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        return (User) attribute;
    }

    /**
     * 根据标签列表查询用户（内存查询）
     * @param tags 要查询的标签列表字符串
     * @return 脱敏后的用户列表
     */
    @Override
    public List<User> searchUserByTagListByCPU(String tags) {

        Page<User> page = new Page<>(1, 1000);

        // 内存中查找
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 查询所有用户
        List<User> userList = userMapper.selectPage(page, queryWrapper).getRecords();
        if (StringUtils.isBlank(tags)) return userList;
        // 将字符串转换为列表对象
        List<String> tagList = Arrays.asList(tags.split(","));
        // Gson对象
        Gson gson = new Gson();
        // 过滤不包含所有标签条件的用户
        return userList.stream().filter(user -> {
            // 用户名匹配
            if (StringUtils.isNotBlank(user.getUsername()) && user.getUsername().contains(tags)) return true;
            // 标签列表为空
            if (StringUtils.isBlank(user.getTags())) return false;
            Set<String> tempTagList = gson.fromJson(user.getTags(), new TypeToken<Set<String>>() {
            }.getType());
            for (String tag: tagList) {
                if (!tempTagList.contains(tag)) return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * 分页查询（推荐用户）
     * @param page  当前查询第几页
     * @param size  每页查询多少条数据
     * @return
     */
    @Override
    public IPage<User> recommend(int page, int size, HttpServletRequest request) {
        // 判断当前用户是否登录
        User currentUser = getCurrentUser(request);
        String redisKey = String.format("paperLeap:user:recommend:%s", currentUser.getId());
        ValueOperations<String, Object> stringObjectValueOperations = redisTemplate.opsForValue();

        // 如果有缓存，直接读缓存
        IPage<User> userIPage = (IPage<User>) stringObjectValueOperations.get(redisKey);
        if (userIPage != null) {
            return userIPage;
        }
        // 无缓存，查询数据库
        Page<User> userPage = new Page<>(page, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userIPage = userMapper.selectPage(userPage, queryWrapper);
        // 写缓存
        try {
            stringObjectValueOperations.set(redisKey, userIPage, 10000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return userIPage;
    }

    /**
     * 修改用户信息
     * @param user      要修改的用户信息
     * @param request   请求头对象
     * @return
     */
    @Override
    public boolean updateUser(User user, HttpServletRequest request) {
        // 用户未登录
        User loginUser = getCurrentUser(request);
        if (loginUser == null) {
            throw new BusinessException(CodeError.NOT_LOGIN);
        }
        // 判断是否是管理员（管理员可以修改所有用户）
        boolean admin = isAdmin(request);
        if (!admin && !loginUser.getId().equals(user.getId())) {
            throw new BusinessException(CodeError.NO_AUTH);
        }
        int res = userMapper.updateById(user);

        return res == 1;
    }
}




