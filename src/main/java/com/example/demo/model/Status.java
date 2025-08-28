package com.example.demo.model;

public enum Status {
  INTERESTED("ToMore"),
  PURCHASED("ToDo"),
  WORKING("Now!!"),
  COMPLETED("完了");

  private final String displayName;

  Status(String displayName){
    this.displayName = displayName;
  }

  public String getDisplayName(){
    return displayName;
  }
}
