package com.chatbot.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.chatbot.model.Conversacion;

public class ConversacionDAO {

    public void guardarContexto(
            String usuario,
            int productoId,
            String intencion
    ) {

        String sql = """
        INSERT INTO conversaciones
        (usuario, ultimo_producto_id, ultima_intencion)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE
            ultimo_producto_id = VALUES(ultimo_producto_id),
            ultima_intencion = VALUES(ultima_intencion)
        """;

        try (
                Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, usuario);
            ps.setInt(2, productoId);
            ps.setString(3, intencion);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error guardando conversación",
                    e
            );
        }
    }

    public Conversacion obtener(String usuario) {

        String sql = """
        SELECT ultimo_producto_id,
               ultima_intencion
        FROM conversaciones
        WHERE usuario = ?
        """;

        try (
                Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, usuario);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Conversacion c = new Conversacion();

                    c.setUltimoProductoId(
                            rs.getInt("ultimo_producto_id")
                    );

                    c.setUltimaIntencion(
                            rs.getString("ultima_intencion")
                    );

                    return c;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error obteniendo conversación",
                    e
            );
        }

        return null;
    }
}