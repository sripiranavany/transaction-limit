package com.sripiranavan.transactionlimit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import com.rabbitmq.client.Channel;
import com.sripiranavan.transactionlimit.config.AppConfig;
import com.sripiranavan.transactionlimit.config.RabbitMQConfig;
import com.sripiranavan.transactionlimit.model.Transaction;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.TimeUnit;

@Service
public class RabbitMQConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private final AppConfig appConfig;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private RateLimiter rateLimiter;

    private Cache<String, Long> msisdnCache;

    public RabbitMQConsumer(WebClient.Builder webClientBuilder, AppConfig appConfig) {
        this.appConfig = appConfig;
        this.webClient = webClientBuilder.baseUrl(appConfig.getChargingGatewayBaseurl()).build();
    }

    @PostConstruct
    public void init() {
        if (appConfig.getTps() <= 0) {
            throw new IllegalArgumentException("TPS (transactions per second) must be greater than zero.");
        }

        rateLimiter = RateLimiter.create(appConfig.getTps());
        msisdnCache = CacheBuilder.newBuilder()
                .expireAfterWrite(appConfig.getMsisdnDelayMillis(), TimeUnit.MILLISECONDS)
                .build();
    }


    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processTransaction(Message message, Channel channel) {
        rateLimiter.acquire();
        LOGGER.debug("Rate limiter acquired for transaction processing.");

        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            String transactionJson = new String(message.getBody());
            Transaction transaction = objectMapper.readValue(transactionJson, Transaction.class);

            if (canProcessTransaction(transaction.getMsisdn())) {
                LOGGER.info("Processing transaction for msisdn {}: {}", transaction.getMsisdn(), transaction);

                handleTransaction(transaction);

                channel.basicAck(deliveryTag, false);
            } else {
                long timeLeft = appConfig.getMsisdnDelayMillis() - (System.currentTimeMillis() - msisdnCache.getIfPresent(transaction.getMsisdn()));
                LOGGER.debug("Skipping transaction for msisdn {} due to processing delay. Retry after {} ms.",
                        transaction.getMsisdn(), timeLeft);
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (Exception e) {
            LOGGER.error("Error processing message: {}", e.getMessage());

            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                LOGGER.error("Error rejecting message: {}", ex.getMessage());
            }
        }
    }

    private void handleTransaction(Transaction transaction) {
        LOGGER.info("Processed transaction: {}", transaction);
        webClient.post()
                .uri(appConfig.getChargingGatewayPath())
                .bodyValue(transaction)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> LOGGER.info("Successfully charged MSISDN {}: {}", transaction.getMsisdn(), response))
                .doOnError(error -> LOGGER.error("Failed to charge MSISDN {}: {}", transaction.getMsisdn(), error.getMessage()))
                .subscribe();
    }

    private boolean canProcessTransaction(String msisdn) {
        long currentTime = System.currentTimeMillis();
        Long lastProcessedTime = msisdnCache.getIfPresent(msisdn);
        if (lastProcessedTime == null || currentTime - lastProcessedTime > appConfig.getMsisdnDelayMillis()) {
            msisdnCache.put(msisdn, currentTime);
            return true;
        }
        return false;
    }
}