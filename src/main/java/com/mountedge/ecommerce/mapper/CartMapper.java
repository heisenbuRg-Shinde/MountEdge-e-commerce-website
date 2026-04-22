package com.mountedge.ecommerce.mapper;

import com.mountedge.ecommerce.dto.CartDto;
import com.mountedge.ecommerce.dto.CartItemDto;
import com.mountedge.ecommerce.entity.Cart;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    private final ProductMapper productMapper;

    public CartMapper(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public CartDto toDto(Cart cart) {
        if (cart == null) return null;

        List<CartItemDto> itemDtos = cart.getItems().stream().map(item -> {
            BigDecimal itemTotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return new CartItemDto(
                    item.getCartItemId(),
                    productMapper.toDto(item.getProduct()),
                    item.getQuantity(),
                    itemTotal
            );
        }).collect(Collectors.toList());

        BigDecimal cartTotal = itemDtos.stream()
                .map(CartItemDto::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDto(cart.getCartId(), itemDtos, cartTotal);
    }
}
