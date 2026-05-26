package com.chatbot.dao;

import com.chatbot.model.Producto;

import java.math.BigDecimal;
import java.sql.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    private Producto map(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getBigDecimal("precio"),
                rs.getInt("stock"),
                rs.getString("descripcion"),
                rs.getBoolean("estado")
        );
    }

    public List<Producto> listarActivos() {
        List<Producto> lista = new ArrayList<>();

        String sql = "SELECT id, nombre, precio, stock, descripcion, estado " +
                     "FROM productos WHERE estado = 1 ORDER BY id DESC";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando productos", e);
        }

        return lista;
    }

    public int contarActivos() {

        String sql = "SELECT COUNT(*) FROM productos WHERE estado = 1";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getInt(1);

        } catch (SQLException e) {
            throw new RuntimeException("Error contando productos", e);
        }
    }

    public Producto buscarPorId(int id) {

        String sql = "SELECT id, nombre, precio, stock, descripcion, estado " +
                     "FROM productos WHERE id = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return map(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando producto", e);
        }

        return null;
    }

    public void guardar(Producto p) {

        String sql = "INSERT INTO productos(nombre, precio, stock, descripcion, estado) " +
                     "VALUES(?,?,?,?,?)";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());

            ps.setBigDecimal(
                    2,
                    p.getPrecio() == null ? BigDecimal.ZERO : p.getPrecio()
            );

            ps.setInt(3, p.getStock());

            ps.setString(4, p.getDescripcion());

            ps.setBoolean(5, p.isActivo());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error guardando producto", e);
        }
    }

    public void actualizar(Producto p) {

        String sql = "UPDATE productos " +
                     "SET nombre=?, precio=?, stock=?, descripcion=?, estado=? " +
                     "WHERE id=?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());

            ps.setBigDecimal(
                    2,
                    p.getPrecio() == null ? BigDecimal.ZERO : p.getPrecio()
            );

            ps.setInt(3, p.getStock());

            ps.setString(4, p.getDescripcion());

            ps.setBoolean(5, p.isActivo());

            ps.setInt(6, p.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando producto", e);
        }
    }

    public void eliminar(int id) {

        String sql = "UPDATE productos SET estado = 0 WHERE id = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando producto", e);
        }
    }

    public Producto buscarCoincidencia(String mensaje) {

    String texto = normalizar(mensaje);

    Producto mejorProducto = null;
    int mejorScore = Integer.MAX_VALUE;

    for (Producto p : listarActivos()) {

            String nombre = normalizar(p.getNombre());

            String[] palabrasUsuario = texto.split("\\s+");
            String[] palabrasProducto = nombre.split("\\s+");

            for (String palabraUsuario : palabrasUsuario) {

                for (String palabraProducto : palabrasProducto) {

                    int distancia = distanciaLevenshtein(
                            palabraUsuario,
                            palabraProducto
                    );

                    if (distancia < mejorScore) {
                        mejorScore = distancia;
                        mejorProducto = p;
                    }
                }
            }
        }

        if (mejorScore <= 4) {
            return mejorProducto;
        }

        return null;
    }

    private String normalizar(String texto) {

        if (texto == null) {
            return "";
        }

        String n = Normalizer.normalize(texto, Normalizer.Form.NFD);

        n = n.replaceAll("\\p{M}", "");

        return n.toLowerCase().trim();
    }

    private int distanciaLevenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {

                int costo = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1
                        ),
                        dp[i - 1][j - 1] + costo
                );
            }
        }

        return dp[a.length()][b.length()];
    }
}