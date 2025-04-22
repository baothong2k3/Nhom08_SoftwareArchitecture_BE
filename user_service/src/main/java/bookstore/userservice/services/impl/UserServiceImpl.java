package bookstore.userservice.services.impl;

import bookstore.userservice.dtos.UserDTO;
import bookstore.userservice.dtos.UserRequest;
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




    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }



    @Override
    public UserDTO findById(Long id) {
//        User user = userRepository.findById(id)
//                .orElseThrow(()-> new ItemNotFoundException("Can not find user with id: " + id));
//        return this.convertToDTO(user);
        return null;
    }

    /**
     * Find all users
     * @return
     */
    @Transactional
    @Override
    public List<UserDTO> findAll() {
//        return userRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
        return null;
    }


    /**
     * Save user
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

        User savedUser =  userRepository.save(user);

        UserRequest response = new UserRequest();
        response.setPhoneNumber(savedUser.getPhoneNumber());
        response.setEnabled(savedUser.isEnabled());
        return response;
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

}
