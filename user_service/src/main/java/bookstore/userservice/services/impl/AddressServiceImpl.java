package bookstore.userservice.services.impl;


import bookstore.userservice.dtos.AddressDTO;
import bookstore.userservice.dtos.AddressRequest;
import bookstore.userservice.dtos.UpdateAddressRequest;
import bookstore.userservice.entities.Address;
import bookstore.userservice.entities.User;
import bookstore.userservice.repositories.AddressRepository;
import bookstore.userservice.repositories.UserRepository;
import bookstore.userservice.services.AddressService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;


    private AddressDTO convertToDTO(Address address) {
        AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);
        return addressDTO;
    }

    private Address convertToEntity(AddressDTO addressDTO) {
        Address address = modelMapper.map(addressDTO, Address.class);
        return address;
    }

    @Override
    public AddressDTO save(AddressDTO addressDTO) {
        Address address = this.convertToEntity(addressDTO);
        address = addressRepository.save(address);
        return this.convertToDTO(address);
    }

    @Override
    public Address addAddress(AddressRequest addressRequest) {
        User user = userRepository.findById(addressRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + addressRequest.getUserId()));

        Address address = Address.builder()
                .address(addressRequest.getAddress())
                .user(user)
                .build();

        return addressRepository.save(address);
    }

    @Override
    public Address updateAddress(UpdateAddressRequest updateAddressRequest) {
        Address address = addressRepository.findById(updateAddressRequest.getAddressId())
                .orElseThrow(() -> new IllegalArgumentException("Address not found with ID: " + updateAddressRequest.getAddressId()));

        address.setAddress(updateAddressRequest.getNewAddress());
        return addressRepository.save(address);
    }

    @Override
    public void deleteAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found with ID: " + addressId));
        addressRepository.delete(address);
    }

    @Override
    public List<AddressDTO> getAddressesByUserId(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        return addresses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Address findById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found with ID: " + addressId));
    }
}
