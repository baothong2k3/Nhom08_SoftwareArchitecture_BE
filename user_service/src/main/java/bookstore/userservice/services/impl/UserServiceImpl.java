package bookstore.userservice.services.impl;

import bookstore.userservice.dtos.UserDTO;
import bookstore.userservice.dtos.UserRequest;
import bookstore.userservice.entities.User;
import bookstore.userservice.exceptions.ItemNotFoundException;
import bookstore.userservice.repositories.UserRepository;
import bookstore.userservice.services.JwtService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import bookstore.userservice.services.UserService;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Cannot find user with id: " + id));
        return convertToDTO(user);
    }

    @Transactional
    @Override
    public List<UserDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Modifying
    @Override
    public UserRequest save(UserRequest userRequest) {
        User user = new User();
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        UserRequest response = new UserRequest();
        response.setPhoneNumber(savedUser.getPhoneNumber());
        response.setEnabled(savedUser.isEnabled());
        return response;
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     * Lấy thông tin người dùng từ token
     */
    @Override
    public UserDTO getUserFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String username = jwtService.extractUsername(token);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ItemNotFoundException("User not found with email: " + username));

        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }
}
