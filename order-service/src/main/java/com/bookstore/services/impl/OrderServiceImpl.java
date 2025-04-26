package com.bookstore.services.impl;

<<<<<<< HEAD
public class UserServiceImpl {
=======
import com.bookstore.services.OrderService;
import com.bookstore.entities.Order;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Override
    public Order createOrder(Order order) {
        return null; // TODO: Implement
    }

    @Override
    public Order getOrderById(Long id) {
        return null; // TODO: Implement
    }

    @Override
    public List<Order> getAllOrders() {
        return null; // TODO: Implement
    }

    @Override
    public void deleteOrder(Long id) {
        // TODO: Implement
    }
>>>>>>> 70eb395 (create docker)
}
