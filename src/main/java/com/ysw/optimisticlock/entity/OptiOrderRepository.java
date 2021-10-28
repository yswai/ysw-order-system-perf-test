package com.ysw.optimisticlock.entity;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptiOrderRepository extends PagingAndSortingRepository<OptiOrder, Long> {

}
