package org.example.tripcostlocalization;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "calculation_records")
public class CalculationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal distance;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal consumption;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal price;

    @Column(name = "total_fuel", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFuel;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(nullable = false, length = 10)
    private String language;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CalculationRecord() {
        this.createdAt = LocalDateTime.now();
    }

    public CalculationRecord(BigDecimal distance,
                             BigDecimal consumption,
                             BigDecimal price,
                             BigDecimal totalFuel,
                             BigDecimal totalCost,
                             String language) {
        this.distance = distance;
        this.consumption = consumption;
        this.price = price;
        this.totalFuel = totalFuel;
        this.totalCost = totalCost;
        this.language = language;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    public BigDecimal getConsumption() {
        return consumption;
    }

    public void setConsumption(BigDecimal consumption) {
        this.consumption = consumption;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTotalFuel() {
        return totalFuel;
    }

    public void setTotalFuel(BigDecimal totalFuel) {
        this.totalFuel = totalFuel;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}