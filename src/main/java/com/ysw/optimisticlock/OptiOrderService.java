package com.ysw.optimisticlock;

import com.ysw.optimisticlock.entity.OptiOrder;
import com.ysw.optimisticlock.entity.OptiOrderRepository;
import com.ysw.optimisticlock.entity.OptiProduct;
import com.ysw.optimisticlock.entity.OptiProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class OptiOrderService {

  @Autowired
  private OptiProductRepository optiProductRepository;

  @Autowired
  private OptiOrderRepository optiOrderRepository;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Retryable(value = {
      ObjectOptimisticLockingFailureException.class
  }, maxAttempts = 999, backoff = @Backoff(delay = 500))
  public void placeOrder(Long productId, Integer correlationId) {
    String productName = "Product A";
    System.out.println(" [x] Received Opti Order '" + correlationId.toString());
    transactionTemplate.execute(tx -> {
      OptiProduct product = optiProductRepository.findById(productId).get();
      OptiOrder order = new OptiOrder();
      order.getProducts().add(product);
      product.getOrders().add(order);
      if (product.getQty() > 0) {
        product.setQty(product.getQty() - 1);
        order.setStatus("CONFIRMED");
      } else {
        order.setStatus("REJECTED");
      }
      optiOrderRepository.save(order);
      optiProductRepository.save(product);
      return order;
    });
  }

}
