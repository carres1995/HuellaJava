package com.huellas.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Automatic database schema initializer.
 *
 * <p>Reads and executes all SQL scripts located in {@code src/main/resources/sql/}
 * to ensure database tables exist before the application starts querying.</p>
 *
 * <p>Usage: Call {@link #initialize()} once at application startup
 * (e.g., in the View constructor, Controller init, or main method).</p>
 *
 * <p>Note: Refer to CONSTITUTION.md §2.1 — MVC Architecture (Controller → Service → Repository → DB)</p>
 */
public class DatabaseInitializer {

    private DatabaseInitializer() {
        // Utility class — prevent instantiation
    }

    /**
     * Executes all SQL scripts found in the {@code sql/} resource directory.
     * Uses {@code IF NOT EXISTS} semantics — safe to call multiple times.
     *
     * <p>Call this method ONCE at application startup:</p>
     * <pre>
     *   DatabaseInitializer.initialize();
     * </pre>
     */
    public static void initialize() {
        String[] scripts = discoverScripts();
        if (scripts.length == 0) {
            System.out.println("⚠️  No SQL scripts found in resources/sql/");
            return;
        }

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String scriptName : scripts) {
                String sql = readScript("sql/" + scriptName);
                if (sql != null && !sql.isBlank()) {
                    stmt.execute(sql);
                    System.out.println("   ✅ Executed: " + scriptName);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Discovers SQL script filenames in the {@code sql/} resource directory.
     */
    private static String[] discoverScripts() {
        try (InputStream is = DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream("sql");
             BufferedReader reader = is != null
                     ? new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                     : null) {

            if (reader == null) return new String[0];

            return reader.lines()
                    .filter(name -> name.endsWith(".sql"))
                    .sorted()
                    .toArray(String[]::new);
        } catch (Exception e) {
            return new String[0];
        }
    }

    /**
     * Reads the full content of a SQL script from the classpath.
     */
    private static String readScript(String resourcePath) {
        try (InputStream is = DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (is == null) return null;
            return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            System.err.println("❌ Could not read: " + resourcePath);
            return null;
        }
    }
}
