package bookstore.userservice.services.impl;

import bookstore.userservice.dtos.UserDTO;
import bookstore.userservice.exceptions.ItemNotFoundException;
import bookstore.userservice.repositories.UserRepository;
import bookstore.userservice.entities.User;
import bookstore.userservice.services.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import java.util.List;
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

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return userDTO;
    }

    private User convertToEntity(UserDTO userDTO) {
        User user = modelMapper.map(userDTO, User.class);
        return user;
    }



    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUserName(String username) {
        return userRepository.existsByUserName(username);
    }

    @Override
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new ItemNotFoundException("Can not find user with id: " + id));
        return this.convertToDTO(user);
    }

    /**
     * Find all users
     * @return
     */
    @Transactional
    @Override
    public List<UserDTO> findAll() {
        return userRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }


    /**
     *  Save user
     * @param userDTO
     * @return
     */
    @Transactional
    @Modifying
    @Override
    public UserDTO save(UserDTO userDTO) {
        User user = this.convertToEntity(userDTO);
        user = userRepository.save(user);
        return this.convertToDTO(user);
    }

}
