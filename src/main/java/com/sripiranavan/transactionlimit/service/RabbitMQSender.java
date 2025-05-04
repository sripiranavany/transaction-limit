package com.sripiranavan.transactionlimit.service;

import com.sripiranavan.transactionlimit.config.RabbitMQConfig;
import com.sripiranavan.transactionlimit.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {

    private RabbitTemplate rabbitTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQSender.class);

    public RabbitMQSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(Transaction transaction) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, transaction);
        LOGGER.info("Sent message to RabbitMQ: {}", transaction);
    }
}
