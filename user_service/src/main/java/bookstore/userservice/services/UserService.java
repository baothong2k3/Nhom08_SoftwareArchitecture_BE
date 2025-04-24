package bookstore.userservice.services;

import bookstore.userservice.dtos.UpdateUserRequest;
import bookstore.userservice.dtos.UserDTO;
import bookstore.userservice.dtos.UserRequest;
import bookstore.userservice.entities.User;

import java.util.List;

public interface UserService {
    public UserRequest save(UserRequest userRequest);


    public boolean existsByPhoneNumber(String phoneNumber);
    public boolean existsByEmail(String email);


    public UserDTO findById(Long id);

    public List<UserDTO> findAll();

    UserDTO updateUser(Long id, UpdateUserRequest updateUserRequest);


}
