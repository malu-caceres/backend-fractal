package com.fractal.backend.controller;

import com.fractal.backend.model.Product;
import com.fractal.backend.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        return optionalProduct.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Product product = optionalProduct.get();
        product.setName(productDetails.getName());
        product.setUnitPrice(productDetails.getUnitPrice());
        product.setStock(productDetails.getStock());

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(savedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        productRepository.delete(optionalProduct.get());
        return ResponseEntity.ok().build();
    }
}
