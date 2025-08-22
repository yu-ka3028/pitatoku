package com.example.demo.controller;
import com.example.demo.model.Dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

@Controller
public class DashboardController {
  //ルーティング
  @RequestMapping("/dashboard")
  public String dashboard() {
  
    return "dashboard";
  }
  @RequestMapping("/add-item")
  public String addItem() {
    return "add-item";
  }

  //追加フォームデータを受け取る
  private List<Dashboard> items = new ArrayList<>();

  @PostMapping("/add-item")
  public String handleForm(
    @RequestParam("item_name") String itemName,
    @RequestParam("status") String status,
    @RequestParam(value = "memo", required = false) String memo
  ){
    String updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

  Dashboard newItem = new Dashboard(itemName, getStatusDisplay(status), memo != null ? memo : "", updatedAt);
  items.add(newItem);
  return "redirect:/dashboard";
  }

  private String getStatusDisplay(String status){
    switch(status){
      case "interested": return "ToMore";
      case "purchased": return "ToDo";
      case "working": return "Now!!";
      default: return status;
    }
  }
}
