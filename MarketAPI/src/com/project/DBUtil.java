package com.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// DB 연결
public class DBUtil {
    public static Connection getConnection() throws SQLException {
    	
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace(); 
        }
        
        String url = "jdbc:mysql://jsy-mysql.c3cqw66y8gcb.ap-northeast-2.rds.amazonaws.com:3306/market?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul";
        String user = "admin";
        String password = "ksj30317";
        return DriverManager.getConnection(url, user, password);
    }
}
