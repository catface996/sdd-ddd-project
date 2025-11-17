package com.catface.orderservice.infrastructure.mq;

/**
 * 消息生产者接口
 * 定义消息发送方法
 */
public interface MessageProducer {
    
    /**
     * 发送消息
     * 
     * @param topic 消息主题/队列名称
     * @param message 消息内容
     */
    void send(String topic, String message);
    
    /**
     * 发送消息（带延迟）
     * 
     * @param topic 消息主题/队列名称
     * @param message 消息内容
     * @param delaySeconds 延迟时间（秒）
     */
    void send(String topic, String message, int delaySeconds);
}
