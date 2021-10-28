/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ysw.queue;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import com.ysw.dao.Order;
import com.ysw.dao.OrderRepository;
import com.ysw.dao.Product;
import com.ysw.dao.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;

/**
 * @author Gary Russell
 * @author Scott Deeg
 * @author Wayne Lund
 */
@RabbitListener(queues = "productB", ackMode = "MANUAL")
public class ProductBReceiver {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@RabbitHandler
	public void receive(String in, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
		String productName = "Product B";
		System.out.println(" [x] Received '" + in + "'");
		transactionTemplate.execute(tx -> {
			Product product = productRepository.findByName(productName).orElseGet(() -> {
				Product p = new Product();
				p.setName(productName);
				p.setQty(5);
				return productRepository.save(p);
			});
			Order order = new Order();
			order.getProducts().add(product);
			product.getOrders().add(order);
			if (product.getQty() > 0) {
				product.setQty(product.getQty() - 1);
				order.setStatus("CONFIRMED");
			} else {
				order.setStatus("REJECTED");
			}
			orderRepository.save(order);
			productRepository.save(product);
			return order;
		});
		channel.basicAck(tag, false);
	}

}
