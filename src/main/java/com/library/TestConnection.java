package com.library;

import java.sql.Connection;

public class TestConnection {

    public static void main(String[] args) {

        try {

            Connection connection =
                    DBConnection.getConnection();

            System.out.println("================================");
            System.out.println("MySQL connection successful!");
            System.out.println("================================");

            connection.close();

        } catch (Exception e) {

            System.out.println("MySQL connection failed!");
            e.printStackTrace();
        }
    }
}