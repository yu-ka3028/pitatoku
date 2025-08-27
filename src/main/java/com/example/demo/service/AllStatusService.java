package com.example.demo.service;

import com.example.demo.model.Status;
import com.example.demo.repository.DashboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

@Service
public class AllStatusService {

    // private static final Logger logger = LoggerFactory.getLogger(AllStatusService.class);
    
    @Autowired
    private DashboardRepository dashboardRepository;
    
    public Map<String, Object> calculateInventoryStatus() {
        long interestedCount = dashboardRepository.countByStatus(Status.INTERESTED);
        long purchasedCount = dashboardRepository.countByStatus(Status.PURCHASED);
        long workingCount = dashboardRepository.countByStatus(Status.WORKING);
        
        String status;
        String statusClass;
        
        if (purchasedCount <= workingCount || purchasedCount == 0) {
            status = "Take Action!";
            statusClass = "down";
        } else if (purchasedCount == workingCount * 2) {
            status = "Nice!!";
            statusClass = "nice";
        } else {
            status = "Take it easy!";
            statusClass = "high";
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("statusClass", statusClass);
        result.put("interestedCount", interestedCount);
        result.put("purchasedCount", purchasedCount);
        result.put("workingCount", workingCount);
        
        // コンソール出力
        // System.out.println("=== 在庫ステータス判定結果 ===");
        // System.out.println("ToMore: " + interestedCount + "件");
        // System.out.println("ToDo: " + purchasedCount + "件");
        // System.out.println("Now!!: " + workingCount + "件");
        // System.out.println("判定結果: " + status + " (" + statusClass + ")");
        // System.out.println("================================");
        
        return result;
    }
}
