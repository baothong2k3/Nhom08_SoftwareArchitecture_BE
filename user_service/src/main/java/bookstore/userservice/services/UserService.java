package bookstore.userservice.services;

import bookstore.userservice.dtos.UpdateUserRequest;
import bookstore.userservice.dtos.UserDTO;
import bookstore.userservice.dtos.UserReponseDTO;
import bookstore.userservice.dtos.UserRequest;
import bookstore.userservice.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    public UserRequest save(UserRequest userRequest);


    public boolean existsByPhoneNumber(String phoneNumber);
    public boolean existsByEmail(String email);


    public UserDTO findById(Long id);

    Page<UserReponseDTO> findAll(Pageable pageable,String phoneNumber);

    UserDTO updateUser(Long id, UpdateUserRequest updateUserRequest);


}
