package com.example.demo.repository;

import com.example.demo.model.Dashboard;
import com.example.demo.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
  Long countByStatus(Status status);
}
