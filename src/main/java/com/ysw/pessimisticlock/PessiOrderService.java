package com.ysw.pessimisticlock;

import com.ysw.dao.Order;
import com.ysw.dao.OrderRepository;
import com.ysw.dao.Product;
import com.ysw.dao.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;

@Service
public class PessiOrderService {

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private EntityManager em;

  @Autowired
  private TransactionTemplate tx;

  @Autowired
  private OrderRepository orderRepository;

  public void placeOrder(Long productId, Integer correlationId) {
    String productName = "Product A";
    System.out.println(" [x] Received Opti Order '" + correlationId.toString());
    tx.execute(status -> {
      Product product = em.find(Product.class, productId, LockModeType.PESSIMISTIC_WRITE);
      Order order = new Order();
      order.getProducts().add(product);
      product.getOrders().add(order);
      if (product.getQty() > 0) {
        product.setQty(product.getQty() - 1);
        order.setStatus("CONFIRMED");
      } else {
        order.setStatus("REJECTED");
      }
      return status;
    });
  }

}
