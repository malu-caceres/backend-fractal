package com.fractal.backend.controller;

import com.fractal.backend.exceptions.ResourceNotFoundException;
import com.fractal.backend.model.OrderDetail;
import com.fractal.backend.model.OrderInput;
import com.fractal.backend.model.Product;
import com.fractal.backend.repository.OrderDetailRepository;
import com.fractal.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fractal.backend.repository.OrderRepository;
import com.fractal.backend.model.Order;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public OrderController(ProductRepository productRepository,
                           OrderRepository orderRepository,
                           OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;

    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        return optionalOrder.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderInput orderInput) {
        List<OrderDetail> inputOrderDetails = orderInput.getOrderDetails();

        // Validate the order details
        for (OrderDetail inputOrderDetail : inputOrderDetails) {
            Optional<Product> optionalProduct = productRepository.findById(inputOrderDetail.getProductId());
            if (optionalProduct.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            //Validate stock
            Product product = optionalProduct.get();
            if (product.getStock() < inputOrderDetail.getQuantity()) {
                return ResponseEntity.badRequest().build();
            }
        }

        // Create the order
        Order order = new Order();
        order.setDate(LocalDate.now());
        order.setStatus(Order.OrderStatus.Pending);
        order.setOrderNumber(orderInput.getOrderNumber());
        Order savedOrder = orderRepository.save(order);

        List<OrderDetail> orderDetails = new ArrayList<>();

        for (OrderDetail inputOrderDetail : inputOrderDetails) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProductId(inputOrderDetail.getProductId());
            orderDetail.setOrderId(savedOrder.getId());
            orderDetail.setOrder(savedOrder);
            orderDetail.setQuantity(inputOrderDetail.getQuantity());
            orderDetails.add(orderDetail);

            // Update the product stock
            Product product = productRepository.findById(inputOrderDetail.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + inputOrderDetail.getProductId()));
            orderDetail.setProduct(product);
            product.setStock(product.getStock() - inputOrderDetail.getQuantity());
            productRepository.save(product);

            //Save order detail
            orderDetailRepository.save(orderDetail);
        }
        order.setOrderDetails(orderDetails);

        return ResponseEntity.ok(savedOrder);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order updatedOrder) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Order order = optionalOrder.get();
        order.setStatus(updatedOrder.getStatus());
        order.setOrderNumber(updatedOrder.getOrderNumber());
        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.ok(savedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        orderRepository.delete(optionalOrder.get());
        return ResponseEntity.noContent().build();
    }
}