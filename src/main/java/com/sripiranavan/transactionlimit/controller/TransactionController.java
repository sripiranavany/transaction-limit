package com.sripiranavan.transactionlimit.controller;

import com.sripiranavan.transactionlimit.model.Transaction;
import com.sripiranavan.transactionlimit.service.RabbitMQSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private RabbitMQSender rabbitMQSender;
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionController.class);

    public TransactionController(RabbitMQSender rabbitMQSender) {
        this.rabbitMQSender = rabbitMQSender;
    }

    @PostMapping
    public ResponseEntity<String> receiveTransaction(@RequestBody Transaction transaction) {
        try {
            LOGGER.info("Received transaction: {}", transaction);
            rabbitMQSender.send(transaction);
            return ResponseEntity.ok("Transaction received and sent to RabbitMQ");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing transaction: " + e.getMessage());
        }
    }
}
