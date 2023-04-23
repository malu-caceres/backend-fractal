package com.fractal.backend.model;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String orderNumber;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails;

    @Transient
    private int numberOfProducts;

    @Transient
    private double finalPrice;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getNumberOfProducts() {
        return orderDetails.size();
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public double getFinalPrice() {
        double total = 0.0;
        for (OrderDetail od : orderDetails) {
            total += od.getQuantity() * od.getProduct().getUnitPrice();
        }
        return total;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public enum OrderStatus {
        Pending,
        InProgress,
        Completed,
    }

}



