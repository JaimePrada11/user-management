package com.jaimeprada.config;

import com.jaimeprada.exceptions.UserNotFoundException;
import com.jaimeprada.model.Role;
import com.jaimeprada.model.User;
import com.jaimeprada.service.UserService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        String url = System.getenv().getOrDefault(
                "DB_URL",
                "jdbc:postgresql://localhost:5432/user_management"
        );

        String user = System.getenv().getOrDefault(
                "DB_USER",
                "postgres"
        );

        String password = System.getenv().getOrDefault(
                "DB_PASSWORD",
                "postgres"
        );

        DatabaseConnection.init(url, user, password);
        AppContext.init();

        seedAdminUser();

    }

    private void seedAdminUser() {

        String adminUsername = System.getenv().getOrDefault("ADMIN_USERNAME", "admin");
        String adminEmail = System.getenv().getOrDefault("ADMIN_EMAIL", "admin@jaimeprada.com");
        String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "Admin");
        String adminFullName = System.getenv().getOrDefault("ADMIN_FULLNAME", "Administrator");

        UserService userService = AppContext.getInstance().getUserService();

        try {
            userService.getUserByUsername(adminUsername);
            System.out.println("Admin user already exists, skipping seed.");
            return;
        } catch (UserNotFoundException e) {

        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPassword(adminPassword);
        admin.setFullName(adminFullName);
        admin.setRole(Role.ADMIN);

        userService.create(admin);

        System.out.println("Admin user created: " + adminUsername);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Context destroyed");
    }
}