package com.ysw;

import com.ysw.optimisticlock.OptiOrderService;
import com.ysw.optimisticlock.entity.OptiProduct;
import com.ysw.optimisticlock.entity.OptiProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@ActiveProfiles("optimistic")
@SpringBootTest
class OptimisticTest {

    @Autowired
    private OptiOrderService optiOrderService;

    @Autowired
    private OptiProductRepository optiProductRepository;

    @Test
    void shouldDeductCorrectQty_withoutConcurrency() throws InterruptedException {
        String productName = "ProductA";
        OptiProduct product = optiProductRepository.findByName(productName).orElseGet(() -> {
            OptiProduct p = new OptiProduct();
            p.setName(productName);
            p.setQty(10);
            return optiProductRepository.save(p);
        });
        int times = 100;
        for (int i = 0 ; i < times ; i++) {
            String message = "Opti takeProductA command User1";
            System.out.println(" [x] Sent '" + message + "'");
            optiOrderService.placeOrder(product.getId(), i);
        }
        new CountDownLatch(1).await();
    }

    @Test
    void shouldDeductCorrectQty_withOptimisticLockingHandling() throws InterruptedException {
        String productName = "ProductA";
        OptiProduct product = optiProductRepository.findByName(productName).orElseGet(() -> {
            OptiProduct p = new OptiProduct();
            p.setName(productName);
            p.setQty(10);
            return optiProductRepository.save(p);
        });
        int times = 100;
        final ExecutorService executor = Executors.newFixedThreadPool(times);
        for (int i = 0 ; i < times ; i++) {
            final AtomicInteger id = new AtomicInteger(i);
            executor.execute(() -> {
                String message = "Opti takeProductA command User1";
                System.out.println(" [x] Sent '" + message + "'");
                optiOrderService.placeOrder(product.getId(), id.get());
            });
        }
        new CountDownLatch(1).await();
        executor.shutdown();
    }
}
