package com.may.paperleap.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.may.paperleap.mapper.UserMapper;
import com.may.paperleap.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author May20242
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L);

    @Resource
    private UserMapper userMapper;

    /**
     * 每天执行，加载预热用户
     */
    @Scheduled(cron = "0 40 11 * * *")
    public void doCacheRecommend() {
        RLock lock = redissonClient.getLock("paperLeap:preCacheJob:doCache:lock");
        try {
            if(lock.tryLock(0, 30000, TimeUnit.MILLISECONDS)) {
                System.out.println("rLock: " + Thread.currentThread().getId());
                ValueOperations<String, Object> stringObjectValueOperations = redisTemplate.opsForValue();
                for(Long userId: mainUserList) {
                    String redisKey = String.format("paperLeap:user:recommend:%s", userId);
                    IPage<User> userIPage = (IPage<User>) stringObjectValueOperations.get(redisKey);
                    if(userIPage != null) {
                        return;
                    }
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> page = new Page<>(1, 10);
                    Page<User> userPage = userMapper.selectPage(page, queryWrapper);
                    try {
                        stringObjectValueOperations.set(redisKey, userPage);
                    } catch (Exception e) {
                        log.error("set redis key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 只能释放自己的锁
            if(lock.isHeldByCurrentThread()) {
                lock.unlock();
                System.out.println("unLock: " + Thread.currentThread().getId());
            }
        }


    }
}
