package com.may.paperleap;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;

/**
 * @author May20242
 */
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;
    @Test
    public void getRedissonTest() {

    }

}
