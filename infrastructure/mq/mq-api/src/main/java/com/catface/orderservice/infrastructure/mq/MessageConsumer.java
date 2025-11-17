package com.catface.orderservice.infrastructure.mq;

/**
 * 消息消费者接口
 * 定义消息消费方法
 */
public interface MessageConsumer {
    
    /**
     * 消费消息
     * 
     * @param topic 消息主题/队列名称
     * @param handler 消息处理器
     */
    void consume(String topic, MessageHandler handler);
    
    /**
     * 消息处理器接口
     */
    @FunctionalInterface
    interface MessageHandler {
        /**
         * 处理消息
         * 
         * @param message 消息内容
         */
        void handle(String message);
    }
}
