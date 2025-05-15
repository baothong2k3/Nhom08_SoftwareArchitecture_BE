package com.bookstore.services;

<<<<<<< HEAD
public class UserService {
=======
import com.bookstore.entities.Order;
import java.util.List;

public interface OrderService {
    Order createOrder(Order order);
    Order getOrderById(Long id);
    List<Order> getAllOrders();
    void deleteOrder(Long id);
>>>>>>> 70eb395 (create docker)
}
