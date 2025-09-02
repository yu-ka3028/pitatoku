package com.example.demo.controller;
import com.example.demo.model.Dashboard;
import com.example.demo.model.Status;
import com.example.demo.repository.DashboardRepository;
import com.example.demo.service.AllStatusService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

  @Autowired
  private DashboardRepository dashboardRepository; 

  @Autowired
  private AllStatusService allStatusService;

  //ルーティング
  @RequestMapping("/dashboard")
  public String dashboard(Model model) {
    // 完了以外のアイテムのみを取得（フィルタリングをDBレベルで実行）
    List<Dashboard> items = dashboardRepository.findByStatusNot(Status.COMPLETED);
    Map<String, Object> statusData = allStatusService.calculateInventoryStatus();
    model.addAttribute("items", items);
    model.addAttribute("statusData", statusData);

    return "dashboard";
  }

  @RequestMapping("/add-item")
  public String addItem() {
    return "add-item";
  }

  @RequestMapping("/edit-item")
  public String editItem(@RequestParam("id") Long id, Model model) {
    Dashboard item = dashboardRepository.findById(id).orElseThrow(() -> new RuntimeException("アイテムが見つかりません"));
    model.addAttribute("item", item);
    return "edit-item";
  }

  //追加フォームデータを受け取る
  @PostMapping("/add-item")
  public String handleForm(
    @RequestParam("item_name") String itemName,
    @RequestParam("status") String status,
    @RequestParam(value = "memo", required = false) String memo
  ){
    LocalDateTime updatedAt = LocalDateTime.now();

  Dashboard newItem = new Dashboard(itemName, getStatus(status), memo != null ? memo : "", updatedAt);
  dashboardRepository.save(newItem);
  return "redirect:/dashboard";
  }

  private Status getStatus(String status){
    switch(status){
      case "interested": return Status.INTERESTED;
      case "purchased": return Status.PURCHASED;
      case "working": return Status.WORKING;
      case "completed": return Status.COMPLETED;
      default: return Status.INTERESTED;
    }
  }

  @PostMapping("/update-item")
  public String updateItem(
    @RequestParam("id") Long id,
    @RequestParam("item_name") String itemName,
    @RequestParam("status") String status,
    @RequestParam(value = "memo", required = false) String memo
  ){
    Dashboard item = dashboardRepository.findById(id).orElseThrow(() -> new RuntimeException("アイテムが見つかりません"));
    item.setItemName(itemName);
    item.setStatus(getStatus(status));
    item.setMemo(memo != null ? memo : "");
    item.setUpdatedAt(LocalDateTime.now());

    dashboardRepository.save(item);
    return "redirect:/dashboard";
  }

  @PostMapping("/delete-item")
  public String deleteItem(@RequestParam("id") Long id){
    dashboardRepository.deleteById(id);
    return "redirect:/dashboard";
  }

  @PostMapping("/complete-item")
  public String completeItem(@RequestParam("id") Long id){
    Dashboard item = dashboardRepository.findById(id).orElseThrow(() -> new RuntimeException("アイテムが見つかりません"));
    item.setStatus(Status.COMPLETED);
    item.setUpdatedAt(LocalDateTime.now());
    dashboardRepository.save(item);
    return "redirect:/dashboard";
  }

  @GetMapping("/api/status/display-name")
  public Map<String, String> getStatusDisplayName(
    @RequestParam String status, 
    @RequestParam String type) {
    
    Status statusEnum = Status.valueOf(status);
    String displayName = statusEnum.getDisplayNameByType(type);
    
    return Map.of("displayName", displayName);
  }
}
