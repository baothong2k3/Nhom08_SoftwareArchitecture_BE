package bookstore.userservice.services.impl;


import bookstore.userservice.entities.Address;
import bookstore.userservice.repositories.AddressRepository;
import bookstore.userservice.services.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl implements AddressService {
	@Autowired
    private AddressRepository addressRepository;
    
	@Override
    public Address save(Address address) {
        return this.addressRepository.save(address);
    }
}
