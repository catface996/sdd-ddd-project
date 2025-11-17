package com.catface.orderservice.infrastructure.cache;

import java.util.Optional;

/**
 * 缓存服务接口
 * 定义基础的缓存操作方法
 */
public interface CacheService {
    
    /**
     * 获取缓存值
     * 
     * @param key 缓存键
     * @return 缓存值，如果不存在则返回 Optional.empty()
     */
    Optional<String> get(String key);
    
    /**
     * 设置缓存值
     * 
     * @param key 缓存键
     * @param value 缓存值
     */
    void set(String key, String value);
    
    /**
     * 设置缓存值（带过期时间）
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param ttlSeconds 过期时间（秒）
     */
    void set(String key, String value, long ttlSeconds);
    
    /**
     * 删除缓存
     * 
     * @param key 缓存键
     * @return 是否删除成功
     */
    boolean delete(String key);
    
    /**
     * 检查缓存是否存在
     * 
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);
}
