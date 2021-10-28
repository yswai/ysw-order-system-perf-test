package com.ysw.optimisticlock.entity;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OptiProductRepository extends PagingAndSortingRepository<OptiProduct, Long> {

  Optional<OptiProduct> findByName(String name);

}
