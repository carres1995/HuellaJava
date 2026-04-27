package com.huellas;

import com.huellas.config.ConnectionFactory;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class TestDB {
    @Test
    public void testEnumValues() {
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT enumlabel FROM pg_enum JOIN pg_type ON pg_enum.enumtypid = pg_type.oid WHERE typname = 'role_t'")) {
            System.out.println("Enum values for role_t:");
            while (rs.next()) {
                System.out.println("--> " + rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
