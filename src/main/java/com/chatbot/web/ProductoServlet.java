package com.chatbot.web;

import com.chatbot.dao.ProductoDAO;
import com.chatbot.model.Producto;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/admin/productos")
public class ProductoServlet extends HttpServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String accion = request.getParameter("accion");
        String id = request.getParameter("id");
        String nombre = request.getParameter("nombre");
        String precio = request.getParameter("precio");
        String stock = request.getParameter("stock");
        String descripcion = request.getParameter("descripcion");

        Producto p = new Producto();
        p.setNombre(nombre);
        p.setPrecio(precio == null || precio.isBlank() ? BigDecimal.ZERO : new BigDecimal(precio));
        p.setStock(stock == null || stock.isBlank() ? 0 : Integer.parseInt(stock));
        p.setDescripcion(descripcion);
        p.setActivo(true);

        if ("eliminar".equalsIgnoreCase(accion)) {
            productoDAO.eliminar(Integer.parseInt(id));
        } else if (id != null && !id.isBlank()) {
            p.setId(Integer.parseInt(id));
            productoDAO.actualizar(p);
        } else {
            productoDAO.guardar(p);
        }

        response.sendRedirect(request.getContextPath() + "/DashboardServlet");
    }
}
