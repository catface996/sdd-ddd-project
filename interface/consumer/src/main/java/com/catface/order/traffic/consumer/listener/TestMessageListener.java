package com.catface.order.traffic.consumer.listener;

import com.catface.order.common.exception.BusinessException;
import com.catface.order.common.exception.SystemException;
import org.springframework.stereotype.Component;

/**
 * 测试消息监听器
 * 用于验证 Consumer 异常处理机制
 */
@Component
public class TestMessageListener {
    
    /**
     * 处理测试消息
     * 根据参数值抛出不同类型的异常
     * 
     * @param messageType 消息类型：business、system、unknown
     */
    public void handleMessage(String messageType) {
        if ("business".equals(messageType)) {
            throw new BusinessException("TEST_BUSINESS_ERROR", "Consumer business exception");
        } else if ("system".equals(messageType)) {
            throw new SystemException("TEST_SYSTEM_ERROR", "Consumer system exception");
        } else {
            throw new RuntimeException("Consumer unexpected exception");
        }
    }
}
