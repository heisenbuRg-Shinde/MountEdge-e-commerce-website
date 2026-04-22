package com.mountedge.ecommerce.mapper;

import com.mountedge.ecommerce.dto.ProductDto;
import com.mountedge.ecommerce.entity.Product;
import com.mountedge.ecommerce.entity.ProductImage;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getInventory() != null) {
            dto.setStockQuantity(product.getInventory().getStockQuantity());
        } else {
            dto.setStockQuantity(0);
        }

        if (product.getImages() != null) {
            dto.setImageUrls(product.getImages().stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
