package bookstore.userservice.services;


import bookstore.userservice.dtos.AddressDTO;
import bookstore.userservice.dtos.AddressRequest;
import bookstore.userservice.dtos.UpdateAddressRequest;
import bookstore.userservice.entities.Address;

import java.util.List;

public interface AddressService {
    public AddressDTO save(AddressDTO addressDTO);
    public Address addAddress(AddressRequest addressRequest);
    Address updateAddress(UpdateAddressRequest updateAddressRequest);
    void deleteAddressById(Long addressId);
    List<AddressDTO> getAddressesByUserId(Long userId);
    Address findById(Long addressId);
}
