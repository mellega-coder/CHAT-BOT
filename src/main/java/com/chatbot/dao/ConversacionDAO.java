package com.chatbot.dao;

import com.chatbot.model.Conversacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConversacionDAO {

    public void guardarContexto(
            String usuarioId,
            int productoId,
            String intencion,
            String categoria,
            String preferenciaPrecio
    ) {

        String sql = """
            INSERT INTO conversaciones(
                usuario_id,
                ultimo_producto_id,
                ultima_intencion,
                ultima_categoria,
                preferencia_precio
            )
            VALUES(?,?,?,?,?)
            ON DUPLICATE KEY UPDATE
                ultimo_producto_id = VALUES(ultimo_producto_id),
                ultima_intencion = VALUES(ultima_intencion),
                ultima_categoria = VALUES(ultima_categoria),
                preferencia_precio = VALUES(preferencia_precio)
            """;

        try (
                Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, usuarioId);
            ps.setInt(2, productoId);
            ps.setString(3, intencion);
            ps.setString(4, categoria);
            ps.setString(5, preferenciaPrecio);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error guardando conversación",
                    e
            );
        }
    }

    public Conversacion obtener(String usuarioId) {

        String sql = """
            SELECT *
            FROM conversaciones
            WHERE usuario_id = ?
            """;

        try (
                Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Conversacion c = new Conversacion();

                    c.setUsuarioId(
                            rs.getString("usuario_id")
                    );

                    c.setUltimoProductoId(
                            rs.getInt("ultimo_producto_id")
                    );

                    c.setUltimaIntencion(
                            rs.getString("ultima_intencion")
                    );

                    c.setUltimaCategoria(
                            rs.getString("ultima_categoria")
                    );

                    c.setPreferenciaPrecio(
                            rs.getString("preferencia_precio")
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