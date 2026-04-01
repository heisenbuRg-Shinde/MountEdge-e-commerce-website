package com.mountedge.ecommerce.config;

import com.mountedge.ecommerce.entity.Category;
import com.mountedge.ecommerce.entity.User;
import com.mountedge.ecommerce.repository.CategoryRepository;
import com.mountedge.ecommerce.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, 
                                     CategoryRepository categoryRepository, 
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
        };
    }
}
