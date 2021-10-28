package com.ysw;

import com.ysw.dao.OrderRepository;
import com.ysw.dao.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ActiveProfiles("queue")
@SpringBootTest
class QueueTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier("productA")
    private Queue queueProductA;

    @Autowired
    @Qualifier("productB")
    private Queue queueProductB;

    @Test
    void shouldDeductCorrectQty_withoutConcurrency() throws InterruptedException {
        int times = 100;
        for (int i = 0 ; i < times ; i++) {
            String message = "takeProductA command User1";
            this.rabbitTemplate.convertAndSend(queueProductA.getName(), message);
            System.out.println(" [x] Sent '" + message + "'");
        }
        new CountDownLatch(1).await();
    }

    @Test
    void shouldDeductCorrectQty_withOptimisticLockingHandling() throws InterruptedException {
        int times = 100;
        final ExecutorService executor = Executors.newFixedThreadPool(times);
        for (int i = 0 ; i < times ; i++) {
            executor.execute(() -> {
                String message = "takeProductA command User1";
                this.rabbitTemplate.convertAndSend(queueProductA.getName(), message);
                System.out.println(" [x] Sent '" + message + "'");
            });
        }
        new CountDownLatch(1).await();
        executor.shutdown();
    }
}
