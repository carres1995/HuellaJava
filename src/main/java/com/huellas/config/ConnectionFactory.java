package com.huellas.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized database connection provider.
 *
 * <p>Production connections use configuration from AppConfig.</p>
 * <p>Test connections use H2 in-memory database.</p>
 */
public class ConnectionFactory {

    // ═══ TEST DATABASE (H2 in-memory) ═══
    private static final String TEST_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String TEST_USER = "sa";
    private static final String TEST_PASSWORD = "";

    private ConnectionFactory() {
        // Utility class — prevent instantiation
    }

    /**
     * Returns a connection to the production database using AppConfig.
     *
     * @return a new {@link Connection} instance
     * @throws SQLException if connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        AppConfig config = AppConfig.getInstance();
        try {
            Class.forName(config.getDbDriver());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver not found: " + config.getDbDriver(), e);
        }
        return DriverManager.getConnection(
            config.getDbUrl(), 
            config.getDbUser(), 
            config.getDbPassword()
        );
    }

    /**
     * Returns a connection to the H2 in-memory test database.
     *
     * @return a new {@link Connection} to H2 in-memory DB
     * @throws SQLException if connection cannot be established
     */
    public static Connection getTestConnection() throws SQLException {
        return DriverManager.getConnection(TEST_URL, TEST_USER, TEST_PASSWORD);
    }
}
