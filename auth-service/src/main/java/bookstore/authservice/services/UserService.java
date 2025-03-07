package bookstore.authservice.services;

import bookstore.authservice.entities.User;

public interface UserService {
    User findByUserName(String userName);
    void saveUser(User user);
    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
}
