package com.huellas.repository;

import com.huellas.config.ConnectionFactory;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Clase base para todos los repositorios JDBC.
 * Proporciona acceso centralizado a la conexión.
 */
public abstract class BaseRepository {

    /**
     * Obtiene una conexión activa desde la fábrica.
     * @return Connection lista para usar.
     * @throws SQLException Si hay error de conexión.
     */
    protected Connection getConnection() throws SQLException {
        return ConnectionFactory.getConnection();
    }
}
