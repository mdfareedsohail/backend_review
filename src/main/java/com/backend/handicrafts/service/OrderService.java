package com.backend.handicrafts.service;

import com.backend.handicrafts.entity.Order;
import com.backend.handicrafts.exception.ResourceNotFoundException;
import com.backend.handicrafts.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order createOrder(Order order) {
        order.setId(null);
        Order savedOrder = orderRepository.save(order);
        log.info("DATA INSERTED SUCCESSFULLY: Order{{id={}, totalAmount={}}}",
                savedOrder.getId(), savedOrder.getTotalAmount());
        return savedOrder;
    }

    public Order updateOrder(Long id, Order payload) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        existing.setTotalAmount(payload.getTotalAmount());
        existing.setStatus(payload.getStatus());

        return orderRepository.save(existing);
    }

    public Order updateOrderStatus(Long id, com.backend.handicrafts.entity.OrderStatus status) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        existing.setStatus(status);
        return orderRepository.save(existing);
    }

    public void deleteOrder(Long id) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        orderRepository.delete(existing);
    }
}
