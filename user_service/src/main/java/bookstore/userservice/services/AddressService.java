package bookstore.userservice.services;


import bookstore.userservice.dtos.AddressDTO;
import bookstore.userservice.entities.Address;

public interface AddressService {
    public AddressDTO save(AddressDTO addressDTO);
}
