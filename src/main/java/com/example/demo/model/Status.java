package com.example.demo.model;

public enum Status {
  INTERESTED("ToMore"),
  PURCHASED("ToDo"),
  WORKING("Now!!");

  private final String displayName;

  Status(String displayName){
    this.displayName = displayName;
  }

  public String getDisplayName(){
    return displayName;
  }
}
