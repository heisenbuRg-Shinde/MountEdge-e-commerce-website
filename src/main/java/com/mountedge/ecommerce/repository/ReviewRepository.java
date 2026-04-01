package com.mountedge.ecommerce.repository;

import com.mountedge.ecommerce.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductProductId(Long productId);
    boolean existsByUserUserIdAndProductProductId(Long userId, Long productId);
}
