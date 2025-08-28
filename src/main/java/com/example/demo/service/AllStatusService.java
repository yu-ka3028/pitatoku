package com.example.demo.service;

import com.example.demo.model.Status;
import com.example.demo.repository.DashboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

@Service
public class AllStatusService {

    private static final Logger logger = LoggerFactory.getLogger(AllStatusService.class);
    
    @Autowired
    private DashboardRepository dashboardRepository;
    
    public Map<String, Object> calculateInventoryStatus() {
        // 完了以外のアイテムのみを対象とする（完了アイテムは在庫計算から除外）
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
        
        // ログ出力（コンソール出力を削減してメモリ使用量を削減）
        logger.info("=== 在庫ステータス判定結果 ===");
        logger.info("ToMore: {}件, ToDo: {}件, Now!!: {}件", interestedCount, purchasedCount, workingCount);
        logger.info("判定結果: {} ({})", status, statusClass);
        logger.info("================================");
        
        return result;
    }
}
