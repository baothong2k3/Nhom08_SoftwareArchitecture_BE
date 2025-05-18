package bookstore.userservice.services.impl;

import bookstore.userservice.dtos.UpdateUserRequest;
import bookstore.userservice.dtos.UserDTO;
import bookstore.userservice.dtos.UserReponseDTO;
import bookstore.userservice.dtos.UserRequest;
import bookstore.userservice.entities.Address;
import bookstore.userservice.exceptions.ItemNotFoundException;
import bookstore.userservice.repositories.UserRepository;
import bookstore.userservice.entities.User;
import bookstore.userservice.services.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    ModelMapper modelMapper;


    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private UserDTO convertToDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Cannot find user with id: " + id));
        return this.convertToDTO(user);
    }

    /**
     * Find all users
     *
     * @return
     */
    @Transactional
    @Override
    public Page<UserReponseDTO> findAll(Pageable pageable) {
        Page<User> users=  userRepository.findAll(pageable);
        return users.map(user -> UserReponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .addresses(user.getAddresses().stream()
                        .map(Address::getAddress)
                        .collect(Collectors.toList()))
                .build());
    }

    /**
     * Save user
     *
     * @param userRequest
     * @return
     */
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

    @Transactional
    @Override
    public UserDTO updateUser(Long id, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Cannot find user with id: " + id));

        // Check if the email is already used by another user
        String newEmail = updateUserRequest.getEmail();

        if (newEmail != null && !newEmail.isBlank() &&
                userRepository.existsByEmail(newEmail) &&
                !Objects.equals(user.getEmail(), newEmail)) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }


        // Update user fields
        user.setFullName(updateUserRequest.getFullName());
        user.setEmail(updateUserRequest.getEmail());
        user.setDob(updateUserRequest.getDob());

        // Save and return updated user
        User updatedUser = userRepository.save(user);
        return this.convertToDTO(updatedUser);
    }
}
