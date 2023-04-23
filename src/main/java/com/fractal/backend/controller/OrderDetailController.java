package com.fractal.backend.controller;

import com.fractal.backend.exceptions.BadRequestException;
import com.fractal.backend.exceptions.ResourceNotFoundException;
import com.fractal.backend.model.OrderDetail;
import com.fractal.backend.model.Product;
import com.fractal.backend.model.Order;
import com.fractal.backend.repository.OrderDetailRepository;
import com.fractal.backend.repository.ProductRepository;
import com.fractal.backend.repository.OrderRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/order-details")
public class OrderDetailController {
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderDetailController(OrderDetailRepository orderDetailRepository,
                                 ProductRepository productRepository,
                                 OrderRepository orderRepository) {
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public List<OrderDetail> getAllOrderDetails() {
        return  orderDetailRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetail> getOrderDetailById(@PathVariable Long id) {
        Optional<OrderDetail> orderDetail = orderDetailRepository.findById(id);
        return orderDetail.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderDetail> createOrderDetail(@RequestBody OrderDetail orderDetail) {
        Product product = productRepository.findById(orderDetail.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + orderDetail.getProduct().getId()));

        Order order = orderRepository.findById(orderDetail.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderDetail.getOrder().getId()));

        if (orderDetail.getQuantity() > product.getStock()) {
            throw new BadRequestException("Insufficient stock for product with id: " + product.getId());
        }

        product.setStock(product.getStock() - orderDetail.getQuantity());
        productRepository.save(product);

        orderDetail.setProduct(product);
        orderDetail.setOrder(order);

        OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);
        return new ResponseEntity<>(savedOrderDetail, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDetail> updateOrderDetail(@PathVariable Long id, @RequestBody OrderDetail updatedOrderDetail) {
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderDetail not found with id: " + id));

        Product product = productRepository.findById(updatedOrderDetail.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + updatedOrderDetail.getProduct().getId()));

        Order order = orderRepository.findById(updatedOrderDetail.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + updatedOrderDetail.getOrder().getId()));

        if (updatedOrderDetail.getQuantity() > product.getStock() + orderDetail.getQuantity()) {
            throw new BadRequestException("Insufficient stock for product with id: " + product.getId());
        }

        product.setStock(product.getStock() + orderDetail.getQuantity() - updatedOrderDetail.getQuantity());
        productRepository.save(product);

        orderDetail.setProduct(product);
        orderDetail.setOrder(order);
        orderDetail.setQuantity(updatedOrderDetail.getQuantity());

        OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);
        return new ResponseEntity<>(savedOrderDetail, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderDetail(@PathVariable Long id) {
        Optional<OrderDetail> orderDetail = orderDetailRepository.findById(id);
        if (orderDetail.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Product product = orderDetail.get().getProduct();
        product.setStock(product.getStock() + orderDetail.get().getQuantity());
        productRepository.save(product);
        orderDetailRepository.delete(orderDetail.get());
        return ResponseEntity.ok().build();
    }
}

