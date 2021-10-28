# Abstraction

To solve incorrect product count when service receives multiple concurrent order placement requests
Following approaches were considered:
- Adding queue per product, and listener to process order placement in sequential way, 
  any update to product will go through this queue
- Back office order process flow, where order requests are always inserted with timestamp captured.
  Then a backend processing-loop 
  (e.g.,: scheduled process will read the order table order by timestamp and process the orders 
  (thus calculating quantity) sequentially, and update order status accordingly)
- Use JPQL or HQL or NativeQuery to execute the update, since single UPATE query is atomic.
```
  UPDATE product as p
  SET qty = qty + :qtyOrdered
  FROM order as o
  ON o.product_id = p.id
  WHERE o.id = :orderId
```
- Optimistic lock ```@Version``` added to product entity, and retry on ```ObjectOptimisticLockingFailureException``` 
- Pessimistic lock:
```      EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Item item = em.find(Item.class, id, LockModeType.PESSIMISTIC_WRITE);
        item.setAmount(item.getAmount() + amount);
        em.flush();
        em.getTransaction().commit();
```
or
```
  @Lock(LockModeType.PESSIMISTIC_READ)
  public Optional<Product> findById(Long productId);
```
or to extend lock to multiple entities, use lock scope but not all persistent providers supports this
```
        // EXTENDED SCOPE
        Map<String, Object> map = new HashMap<>();
        map.put("javax.persistence.lock.scope", PessimisticLockScope.EXTENDED);

        EntityManager em3 = getEntityManagerWithOpenTransaction();
        product = em3.find(Product.class, 1L, LockModeType.PESSIMISTIC_WRITE, map);`
```

## Steps
- Run with profiles
  ```--spring.profiles.active=queue```
  ```--spring.profiles.active=optimistic```
  ```--spring.profiles.active=pessimistic```
- Tested with rabbitmq version 3.9.7 (single instance)
- Queue per product, Manual consumer ack, Connection Channel size = 1
- TODO: test with higher load & multiple replicas, dynamic queue definition and listener registration
