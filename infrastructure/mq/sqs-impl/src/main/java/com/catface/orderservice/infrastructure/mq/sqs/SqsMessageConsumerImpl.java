package com.catface.orderservice.infrastructure.mq.sqs;

import com.catface.orderservice.infrastructure.mq.MessageConsumer;
import org.springframework.stereotype.Component;

/**
 * SQS 消息消费者实现类（空实现）
 * 后续根据实际需求实现具体的 SQS 连接和消费逻辑
 */
@Component
public class SqsMessageConsumerImpl implements MessageConsumer {
    
    @Override
    public void consume(String topic, MessageHandler handler) {
        // TODO: 实现 SQS 消息消费
    }
}
