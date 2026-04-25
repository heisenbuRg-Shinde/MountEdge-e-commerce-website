package com.mountedge.ecommerce.service;

import com.mountedge.ecommerce.dto.ProductDto;
import com.mountedge.ecommerce.dto.ProductFilterDto;
import com.mountedge.ecommerce.entity.Category;
import com.mountedge.ecommerce.entity.Inventory;
import com.mountedge.ecommerce.entity.Product;
import com.mountedge.ecommerce.entity.ProductImage;
import com.mountedge.ecommerce.exception.ResourceNotFoundException;
import com.mountedge.ecommerce.mapper.ProductMapper;
import com.mountedge.ecommerce.repository.CategoryRepository;
import com.mountedge.ecommerce.repository.InventoryRepository;
import com.mountedge.ecommerce.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductMapper productMapper;
    private final String UPLOAD_DIR = "src/main/resources/static/images/products/";

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, InventoryRepository inventoryRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
        this.productMapper = productMapper;
    }

    public Page<ProductDto> getProducts(ProductFilterDto filterDto, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterDto.getName() != null && !filterDto.getName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filterDto.getName().toLowerCase() + "%"));
            }

            if (filterDto.getCategory() != null && !filterDto.getCategory().isEmpty()) {
                predicates.add(cb.equal(root.get("category").get("name"), filterDto.getCategory()));
            }

            if (filterDto.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filterDto.getMinPrice()));
            }

            if (filterDto.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filterDto.getMaxPrice()));
            }

            predicates.add(cb.equal(root.get("active"), true));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable).map(productMapper::toDto);
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto createProduct(ProductDto productDto, List<MultipartFile> files) throws IOException {
        Category category = categoryRepository.findByName(productDto.getCategoryName())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = new Product(category, productDto.getName(), productDto.getDescription(), productDto.getPrice());
        if (productDto.getBestSeller() != null) {
            product.setBestSeller(productDto.getBestSeller());
        }
        
        Inventory inventory = new Inventory(product, productDto.getStockQuantity());
        product.setInventory(inventory);

        // Handle Images
        if (files != null && !files.isEmpty()) {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            boolean isFirst = true;
            for (MultipartFile file : files) {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);

                ProductImage image = new ProductImage(product, "/images/products/" + fileName, isFirst);
                product.addImage(image);
                isFirst = false;
            }
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }
    @Transactional
    public ProductDto updateProduct(Long id, ProductDto productDto, List<MultipartFile> files) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Category category = categoryRepository.findByName(productDto.getCategoryName())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCategory(category);
        if (productDto.getBestSeller() != null) {
            product.setBestSeller(productDto.getBestSeller());
        }

        if (product.getInventory() != null) {
            product.getInventory().setStockQuantity(productDto.getStockQuantity());
        } else {
            Inventory inventory = new Inventory(product, productDto.getStockQuantity());
            product.setInventory(inventory);
        }

        // Handle Images (appending new ones)
        if (files != null && !files.isEmpty()) {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);

                ProductImage image = new ProductImage(product, "/images/products/" + fileName, false);
                product.addImage(image);
            }
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }
}
