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
    
    private static final int NICE_RATIO = 2;
    private static final String TAKE_ACTION = "Take Action!";
    private static final String TAKE_ACTION_CLASS = "down";
    private static final String NICE = "Nice!!";
    private static final String NICE_CLASS = "nice";
    private static final String TAKE_EASY = "Take it easy!";
    private static final String TAKE_EASY_CLASS = "high";
    
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
            status = TAKE_ACTION;
            statusClass = TAKE_ACTION_CLASS;
        } else if (purchasedCount == workingCount * NICE_RATIO) {
            status = NICE;
            statusClass = NICE_CLASS;
        } else {
            status = TAKE_EASY;
            statusClass = TAKE_EASY_CLASS;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("statusClass", statusClass);
        result.put("interestedCount", interestedCount);
        result.put("purchasedCount", purchasedCount);
        result.put("workingCount", workingCount);
        
        logger.info("=== 在庫ステータス判定結果 ===");
        logger.info("ToMore: {}件, ToDo: {}件, Now!!: {}件", interestedCount, purchasedCount, workingCount);
        logger.info("判定結果: {} ({})", status, statusClass);
        logger.info("================================");
        
        return result;
    }
}
