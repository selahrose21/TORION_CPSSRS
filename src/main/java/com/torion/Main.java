package com.torion;

import com.torion.dao.DatabaseInitializer;
import com.torion.dao.UserDao;
import com.torion.ui.Authentication;
import java.util.Scanner;

public class Main {

    public static void main (String args[]){

        DatabaseInitializer.initialize();

        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();

        Authentication auth = new Authentication(userDao, scanner);
        auth.start();
        scanner.close();

    }
}