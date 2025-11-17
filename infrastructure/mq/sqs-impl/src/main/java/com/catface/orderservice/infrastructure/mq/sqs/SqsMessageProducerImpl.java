package com.catface.orderservice.infrastructure.mq.sqs;

import com.catface.orderservice.infrastructure.mq.MessageProducer;
import org.springframework.stereotype.Component;

/**
 * SQS 消息生产者实现类（空实现）
 * 后续根据实际需求实现具体的 SQS 连接和发送逻辑
 */
@Component
public class SqsMessageProducerImpl implements MessageProducer {
    
    @Override
    public void send(String topic, String message) {
        // TODO: 实现 SQS 消息发送
    }
    
    @Override
    public void send(String topic, String message, int delaySeconds) {
        // TODO: 实现 SQS 延迟消息发送
    }
}
