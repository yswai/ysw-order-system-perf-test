package com.ysw;

import com.ysw.dao.OrderRepository;
import com.ysw.dao.Product;
import com.ysw.dao.ProductRepository;
import com.ysw.pessimisticlock.PessiOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@ActiveProfiles("optimistic")
@SpringBootTest
class PessimisticTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PessiOrderService pessiOrderService;

    @Autowired
    private TransactionTemplate tx;

    @Test
    void shouldDeductCorrectQty_withoutConcurrency() throws InterruptedException {
        String productName = "ProductA";
        Long productId = tx.execute(status -> {
            Product persisted = productRepository.findByName(productName).orElseGet(() -> {
                Product p = new Product();
                p.setName(productName);
                p.setQty(10);
                return productRepository.save(p);
            });
            return persisted.getId();
        });
        int times = 100;
        for (int i = 0 ; i < times ; i++) {
            String message = "Opti takeProductA command User1";
            System.out.println(" [x] Sent '" + message + "'");
            pessiOrderService.placeOrder(productId, i);
        }
        new CountDownLatch(1).await();;
    }

    @Test
    void shouldDeductCorrectQty_withOptimisticLockingHandling() throws InterruptedException {
        String productName = "ProductA";
        Long productId = tx.execute(status -> {
            Product persisted = productRepository.findByName(productName).orElseGet(() -> {
                Product p = new Product();
                p.setName(productName);
                p.setQty(10);
                return productRepository.save(p);
            });
            return persisted.getId();
        });
        int times = 100;
        final ExecutorService executor = Executors.newFixedThreadPool(times);
        for (int i = 0 ; i < times ; i++) {
            final AtomicInteger id = new AtomicInteger(i);
            executor.execute(() -> {
                String message = "Opti takeProductA command User1";
                System.out.println(" [x] Sent '" + message + "'");
                pessiOrderService.placeOrder(productId, id.get());
            });
        }
        new CountDownLatch(1).await();
        executor.shutdown();
    }
}
