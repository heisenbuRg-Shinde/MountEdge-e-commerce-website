package com.mountedge.ecommerce.service;

import com.mountedge.ecommerce.dto.ProductDto;
import com.mountedge.ecommerce.dto.WishlistDto;
import com.mountedge.ecommerce.entity.Product;
import com.mountedge.ecommerce.entity.User;
import com.mountedge.ecommerce.entity.Wishlist;
import com.mountedge.ecommerce.entity.WishlistItem;
import com.mountedge.ecommerce.exception.ResourceNotFoundException;
import com.mountedge.ecommerce.mapper.ProductMapper;
import com.mountedge.ecommerce.repository.ProductRepository;
import com.mountedge.ecommerce.repository.UserRepository;
import com.mountedge.ecommerce.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public WishlistService(WishlistRepository wishlistRepository, UserRepository userRepository, ProductRepository productRepository, ProductMapper productMapper) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public WishlistDto getUserWishlist(String email) {
        Wishlist wishlist = wishlistRepository.findByUserEmail(email)
                .orElseGet(() -> createWishlistForUser(email));

        List<ProductDto> items = wishlist.getItems().stream()
                .map(item -> productMapper.toDto(item.getProduct()))
                .collect(Collectors.toList());

        return new WishlistDto(wishlist.getWishlistId(), items);
    }

    @Transactional
    public void addProductToWishlist(String email, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUserEmail(email)
                .orElseGet(() -> createWishlistForUser(email));

        boolean exists = wishlist.getItems().stream()
                .anyMatch(item -> item.getProduct().getProductId().equals(productId));

        if (!exists) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            WishlistItem item = new WishlistItem(wishlist, product);
            wishlist.addItem(item);
            wishlistRepository.save(wishlist);
        }
    }

    @Transactional
    public void removeProductFromWishlist(String email, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));

        // Use a list iterator or filter and remove specifically to ensure orphan removal works
        List<WishlistItem> toRemove = wishlist.getItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(productId))
                .collect(Collectors.toList());
        
        for (WishlistItem item : toRemove) {
            wishlist.removeItem(item);
        }
        
        wishlistRepository.save(wishlist);
    }

    @Transactional
    public Wishlist createWishlistForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Wishlist newWishlist = new Wishlist(user);
        return wishlistRepository.save(newWishlist);
    }
}
