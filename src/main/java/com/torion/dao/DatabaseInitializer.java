package com.torion.dao;

public class DatabaseInitializer {
    public static void initialize() {
        new UserDao();
        System.out.println(" -> Database tables verified");
    }
}
