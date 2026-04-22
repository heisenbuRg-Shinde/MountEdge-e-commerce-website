package com.mountedge.ecommerce.config;

import com.mountedge.ecommerce.entity.Category;
import com.mountedge.ecommerce.entity.Inventory;
import com.mountedge.ecommerce.entity.Product;
import com.mountedge.ecommerce.entity.User;
import com.mountedge.ecommerce.repository.CategoryRepository;
import com.mountedge.ecommerce.repository.InventoryRepository;
import com.mountedge.ecommerce.repository.ProductRepository;
import com.mountedge.ecommerce.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, 
                                     CategoryRepository categoryRepository,
                                     ProductRepository productRepository,
                                     InventoryRepository inventoryRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail("admin@mountedge.com")) {
                User admin = new User("Admin", "admin@mountedge.com", 
                                      passwordEncoder.encode("admin123"), "ROLE_ADMIN");
                userRepository.save(admin);
                System.out.println("Admin user created: admin@mountedge.com / admin123");
            }

            if (categoryRepository.count() == 0) {
                categoryRepository.save(new Category("Electronics"));
                categoryRepository.save(new Category("Clothing"));
                categoryRepository.save(new Category("Home & Decor"));
            }

            if (productRepository.count() == 0) {
                Category cat1 = categoryRepository.findAll().get(0);
                Category cat2 = categoryRepository.findAll().get(1);
                Category cat3 = categoryRepository.findAll().get(2);

                Product p1 = new Product(cat1, "Wireless Headphones", "High quality noise cancelling headphones", new BigDecimal("19999.00"));
                productRepository.save(p1);
                inventoryRepository.save(new Inventory(p1, 50));

                Product p2 = new Product(cat2, "Cotton T-Shirt", "100% Cotton breathable t-shirt", new BigDecimal("1999.00"));
                productRepository.save(p2);
                inventoryRepository.save(new Inventory(p2, 200));

                Product p3 = new Product(cat3, "Ceramic Mug", "Handcrafted ceramic mug", new BigDecimal("1450.00"));
                productRepository.save(p3);
                inventoryRepository.save(new Inventory(p3, 100));
                
                System.out.println("Default products seeded.");
            }
        };
    }
}
