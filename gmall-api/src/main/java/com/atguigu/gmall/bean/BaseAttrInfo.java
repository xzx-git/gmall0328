package com.atguigu.gmall.bean;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

public class BaseAttrInfo implements Serializable {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column
  private String id;
  @Column
  private String attrName;
  @Column
  private String catalog3Id;
  @Column
  private String isEnabled;

  @Transient
  List<BaseAttrValue> attrValueList;

  public List<BaseAttrValue> getAttrValueList() {
    return attrValueList;
  }

  public void setAttrValueList(List<BaseAttrValue> attrValueList) {
    this.attrValueList = attrValueList;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }


  public String getAttrName() {
    return attrName;
  }

  public void setAttrName(String attrName) {
    this.attrName = attrName;
  }


  public String getCatalog3Id() {
    return catalog3Id;
  }

  public void setCatalog3Id(String catalog3Id) {
    this.catalog3Id = catalog3Id;
  }


  public String getIsEnabled() {
    return isEnabled;
  }

  public void setIsEnabled(String isEnabled) {
    this.isEnabled = isEnabled;
  }

}
