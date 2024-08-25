package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnector {
    static final String DB_URL = "jdbc:mysql://localhost:3306/";
    static final String USER = "root";
    static final String PASS = "password for MySql Server";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stat = conn.createStatement()) {
            String sql = "CREATE DATABASE IF NOT EXISTS OOPPROJECT";
            stat.executeUpdate(sql);
            System.out.println("DATABASE CREATED SUCCESSFULLY...");

            String useDB = "USE OOPPROJECT";
            stat.executeUpdate(useDB);

            String createUserTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL, " +
                    "password VARCHAR(50) NOT NULL)";
            stat.executeUpdate(createUserTable);

            String createNoteTable = "CREATE TABLE IF NOT EXISTS notes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "title VARCHAR(100), " +
                    "content TEXT, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id))";
            stat.executeUpdate(createNoteTable);

            System.out.println("TABLES CREATED SUCCESSFULLY...");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
