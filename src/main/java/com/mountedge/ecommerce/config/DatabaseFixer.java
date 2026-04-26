package com.mountedge.ecommerce.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFixer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            jdbcTemplate.execute("ALTER TABLE orders MODIFY status VARCHAR(50)");
            jdbcTemplate.execute("ALTER TABLE order_status_history MODIFY status VARCHAR(50)");
            System.out.println("DatabaseFixer: Successfully updated status columns to VARCHAR(50)");
        } catch (Exception e) {
            System.err.println("DatabaseFixer: Failed to update schema: " + e.getMessage());
        }
    }
}
