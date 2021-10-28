package com.ysw.optimisticlock.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class OptiOrder extends OptiBaseEntity {

  @Id
  @GeneratedValue
  private Long id;

  @Column
  private String status;

  @ManyToMany(mappedBy = "orders")
  private List<OptiProduct> products = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public List<OptiProduct> getProducts() {
    return products;
  }

  public void setProducts(List<OptiProduct> products) {
    this.products = products;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
