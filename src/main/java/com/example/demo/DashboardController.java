package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

@Controller
public class DashboardController {

  @RequestMapping("/dashboard")
  public String dashboard() {
  
    return "dashboard";
  }

  @RequestMapping("/add-item")
  public String addItem() {
    return "add-item";
  }
}
