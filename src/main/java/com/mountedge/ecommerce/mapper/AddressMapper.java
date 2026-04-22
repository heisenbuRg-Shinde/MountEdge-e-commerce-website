package com.mountedge.ecommerce.mapper;

import com.mountedge.ecommerce.dto.AddressDto;
import com.mountedge.ecommerce.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressDto toDto(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressDto(
                address.getAddressId(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.getDefault()
        );
    }
}
