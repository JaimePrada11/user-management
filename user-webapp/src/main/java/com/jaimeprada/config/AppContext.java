package com.jaimeprada.config;

import com.jaimeprada.repository.UserRepositoryImpl;
import com.jaimeprada.service.UserService;
import com.jaimeprada.service.UserServiceImpl;

public class AppContext {

    private static AppContext instance;

    private final UserService userService;

    private AppContext() {
        this.userService = new UserServiceImpl(new UserRepositoryImpl());

    }

    public static void init() {
        if (instance == null) {
            instance = new AppContext();
        }
    }

    public static AppContext getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AppContext not initialized");
        }
        return instance;
    }

    public UserService getUserService() {
        return userService;
    }

}

