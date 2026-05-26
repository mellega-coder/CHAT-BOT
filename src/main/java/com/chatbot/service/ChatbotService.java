package com.chatbot.service;

import com.chatbot.dao.MensajeDAO;
import com.chatbot.dao.ProductoDAO;
import com.chatbot.dao.RespuestaDAO;
import com.chatbot.model.Producto;
import com.chatbot.model.Respuesta;
import com.chatbot.service.OpenAIService;
import java.util.Random;

import java.util.List;

public class ChatbotService {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final RespuestaDAO respuestaDAO = new RespuestaDAO();
    private final MensajeDAO mensajeDAO = new MensajeDAO();
    private final OpenAIService openAIService = new OpenAIService();
    private final Random random = new Random();

    private String saludoAleatorio() {
        String[] saludos = {
                "¡Claro! 😊",
                "¡Sí! 🔥",
                "Perfecto 👍",
                "Buena elección 😎",
                "Excelente opción 🚀",
                "¡Tenemos justo lo que buscas! 🎮"
        };
        return saludos[random.nextInt(saludos.length)];
    }

    public String procesarMensaje(String mensaje) {

        if (mensaje == null || mensaje.isBlank()) {
            return "Escribe un mensaje.";
        }

        String texto = mensaje.toLowerCase();
        String intencion = detectarIntencion(texto);
        Producto producto = productoDAO.buscarCoincidencia(texto);

        if (producto != null) {

            String respuesta;

            switch (intencion) {

                case "PRECIO":
                    respuesta = """
                    %s

                    El producto %s tiene un precio de S/ %.2f 💰

                    Actualmente tenemos %d unidades disponibles.
                    """.formatted(
                            saludoAleatorio(),
                            producto.getNombre(),
                            producto.getPrecio(),
                            producto.getStock()
                );

                break;

                case "STOCK":
                    respuesta = """
                    %s

                    Sí 😊 tenemos disponible:

                    🛒 %s

                    📦 Stock actual: %d unidades
                    """.formatted(
                            saludoAleatorio(),
                            producto.getNombre(),
                            producto.getStock()
                );

                break;

                case "COMPRA":
                    respuesta = """
                    %s

                    Puedes comprar ahora mismo:

                    🛒 %s

                    💰 Precio: S/ %.2f
                    📦 Stock disponible: %d unidades

                    ¿Te gustaría más información? 😄
                    """.formatted(
                            saludoAleatorio(),
                            producto.getNombre(),
                            producto.getPrecio(),
                            producto.getStock()
                );

                break;

                default:
                    respuesta = """
                    %s

                    Tenemos disponible:

                    🛒 %s

                    💰 Precio: S/ %.2f
                    📦 Stock: %d unidades

                    📄 Descripción:
                    %s
                    """.formatted(
                            saludoAleatorio(),
                            producto.getNombre(),
                            producto.getPrecio(),
                            producto.getStock(),
                            producto.getDescripcion()
                );
            }

            mensajeDAO.guardar(mensaje, respuesta);

            return respuesta;
        }

        List<Respuesta> respuestas = respuestaDAO.listarActivos();

        for (Respuesta r : respuestas) {

            String[] palabras = r.getPalabrasClave().split(",");

            for (String palabra : palabras) {

                if (texto.contains(palabra.trim().toLowerCase())) {

                    mensajeDAO.guardar(mensaje, r.getRespuesta());

                    return r.getRespuesta();
                }
            }
        }

        String respuestaIA = openAIService.preguntar(
                """
                Cliente escribió:
                "%s"

                Productos disponibles:
                %s

                Responde como un vendedor amable y profesional.
                """.formatted(
                        mensaje,
                        obtenerProductosTexto()
                )
        );

        mensajeDAO.guardar(mensaje, respuestaIA);

        return respuestaIA;
    }

    private String obtenerProductosTexto() {

        StringBuilder sb = new StringBuilder();

        List<Producto> productos =
                productoDAO.listarActivos();

        for (Producto p : productos) {

            sb.append("""
                    Producto: %s
                    Precio: %s
                    Stock: %d
                    Descripción: %s

                    """.formatted(
                    p.getNombre(),
                    p.getPrecio(),
                    p.getStock(),
                    p.getDescripcion()
            ));
        }

        return sb.toString();
    }

    private String detectarIntencion(String texto) {

        texto = texto.toLowerCase();

        if (
                texto.contains("precio") ||
                texto.contains("cuesta") ||
                texto.contains("vale")
        ) {
            return "PRECIO";
        }

        if (
                texto.contains("stock") ||
                texto.contains("disponible") ||
                texto.contains("hay")
        ) {
            return "STOCK";
        }

        if (
                texto.contains("comprar") ||
                texto.contains("quiero")
        ) {
            return "COMPRA";
        }

        if (
                texto.contains("envio") ||
                texto.contains("delivery")
        ) {
            return "ENVIO";
        }

        return "GENERAL";
    }
}