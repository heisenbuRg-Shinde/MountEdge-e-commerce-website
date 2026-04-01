package com.mountedge.ecommerce.service;

import com.mountedge.ecommerce.dto.ProductDto;
import com.mountedge.ecommerce.entity.Category;
import com.mountedge.ecommerce.entity.Inventory;
import com.mountedge.ecommerce.entity.Product;
import com.mountedge.ecommerce.exception.ResourceNotFoundException;
import com.mountedge.ecommerce.repository.CategoryRepository;
import com.mountedge.ecommerce.repository.InventoryRepository;
import com.mountedge.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return mapToDto(product);
    }

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Category category = categoryRepository.findByName(productDto.getCategoryName())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = new Product(category, productDto.getName(), productDto.getDescription(), productDto.getPrice());
        
        Inventory inventory = new Inventory(product, productDto.getStockQuantity());
        product.setInventory(inventory);
        
        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    private ProductDto mapToDto(Product product) {
        return new ProductDto(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory().getName(),
                product.getInventory() != null ? product.getInventory().getStockQuantity() : 0
        );
    }
}
