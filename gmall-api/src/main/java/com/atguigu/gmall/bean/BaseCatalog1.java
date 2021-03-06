package com.atguigu.gmall.bean;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class BaseCatalog1 implements Serializable {

  @Id
  private String id;
  @Column
  private String name;

  @Override
  public String toString() {
    return "BaseCatalog1{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            '}';
  }

  public BaseCatalog1() {
  }

  public BaseCatalog1(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
