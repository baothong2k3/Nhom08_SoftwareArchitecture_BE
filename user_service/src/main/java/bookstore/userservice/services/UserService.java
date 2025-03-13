package bookstore.userservice.services;

import bookstore.userservice.dtos.UserDTO;
import bookstore.userservice.entities.User;

import java.util.List;

public interface UserService {

    public boolean existsByEmail(String email);

    public boolean existsByUserName(String userName);

    public UserDTO findById(Long id);

    public List<UserDTO> findAll();

    public UserDTO save(UserDTO userDTO);

}
