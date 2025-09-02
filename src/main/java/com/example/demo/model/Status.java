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
  public String getDisplayNameByType(String type){
    switch(type) {
      case "books":
        return getBookDisplayName();
      case "tasks":
        return getTasksDisplayName();
      case "inventory":
        return getInventoryDisplayName();
      default:
        return getDisplayName();
    }
  }
  private String getBookDisplayName(){
    switch(this) {
      case INTERESTED:
        return "未購入";
      case PURCHASED:
        return "購入済み";
      case WORKING:
        return "作業中";
      case COMPLETED:
        return "完了";
      default: return this.displayName;
    }
  }
  private String getTasksDisplayName(){
    switch(this) {
      case INTERESTED:
        return "ToMore";
      case PURCHASED:
        return "ToDo";
      case WORKING:
        return "Now!!";
      case COMPLETED:
        return "完了";
      default: return this.displayName;
    }
  }
  private String getInventoryDisplayName(){
    switch(this) {
      case INTERESTED:
        return "あれば欲しい";
      case PURCHASED:
        return "在庫なし";
      case WORKING:
        return "在庫あり";
      case COMPLETED:
        return "完了";
      default: return this.displayName;
    }
  }
}
