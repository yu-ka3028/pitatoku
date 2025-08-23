package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "dashboard")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "item_name", nullable = false)
  private String itemName;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Status status;

  @Column(name = "memo")
  private String memo;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Dashboard(String itemName, Status status, String memo, LocalDateTime updatedAt){
    this.itemName = itemName;
    this.status = status;
    this.memo = memo;
    this.updatedAt = updatedAt;
  }

}
