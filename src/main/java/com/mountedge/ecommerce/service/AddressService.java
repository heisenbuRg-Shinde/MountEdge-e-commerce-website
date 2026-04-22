package com.mountedge.ecommerce.service;

import com.mountedge.ecommerce.dto.AddressDto;
import com.mountedge.ecommerce.entity.Address;
import com.mountedge.ecommerce.entity.User;
import com.mountedge.ecommerce.exception.ResourceNotFoundException;
import com.mountedge.ecommerce.mapper.AddressMapper;
import com.mountedge.ecommerce.repository.AddressRepository;
import com.mountedge.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository, AddressMapper addressMapper) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.addressMapper = addressMapper;
    }

    public List<AddressDto> getUserAddresses(String email) {
        return addressRepository.findByUserEmail(email).stream()
                .map(addressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDto addAddress(String email, AddressDto addressDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (addressDto.getDefault() != null && addressDto.getDefault()) {
            List<Address> existing = addressRepository.findByUserEmail(email);
            for (Address addr : existing) {
                addr.setDefault(false);
            }
        }

        Address address = new Address(
                user,
                addressDto.getAddressLine1(),
                addressDto.getAddressLine2(),
                addressDto.getCity(),
                addressDto.getState(),
                addressDto.getPincode(),
                addressDto.getDefault() != null ? addressDto.getDefault() : false
        );

        Address saved = addressRepository.save(address);
        return addressMapper.toDto(saved);
    }
}
