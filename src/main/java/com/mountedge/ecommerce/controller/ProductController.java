package com.mountedge.ecommerce.controller;

import com.mountedge.ecommerce.dto.ApiResponse;
import com.mountedge.ecommerce.dto.ProductDto;
import com.mountedge.ecommerce.dto.ProductFilterDto;
import com.mountedge.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getProducts(
            @ModelAttribute ProductFilterDto filterDto,
            Pageable pageable) {
        return ResponseEntity.ok(productService.getProducts(filterDto, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> createProduct(
            @Valid @RequestPart("productDto") ProductDto productDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> files) throws IOException {
        return ResponseEntity.ok(productService.createProduct(productDto, files));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestPart("productDto") ProductDto productDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> files) throws IOException {
        return ResponseEntity.ok(productService.updateProduct(id, productDto, files));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponse(true, "Product deleted successfully"));
    }
}
