package com.mountedge.ecommerce.repository;

import com.mountedge.ecommerce.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUser_UserId(Long userId);
    Optional<Wishlist> findByUserEmail(String email);
}
