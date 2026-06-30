package com.jaimeprada.config;

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

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Context destroyed");
    }
}
