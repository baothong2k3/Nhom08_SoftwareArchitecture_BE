package bookstore.userservice.services.impl;


import bookstore.userservice.dtos.AddressDTO;
import bookstore.userservice.dtos.UserDTO;
import bookstore.userservice.entities.Address;
import bookstore.userservice.entities.User;
import bookstore.userservice.repositories.AddressRepository;
import bookstore.userservice.services.AddressService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl implements AddressService {
	@Autowired
    private AddressRepository addressRepository;

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
}
