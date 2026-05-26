package com.chatbot.service;

import com.chatbot.dao.ConversacionDAO;
import com.chatbot.dao.MensajeDAO;
import com.chatbot.dao.ProductoDAO;
import com.chatbot.dao.RespuestaDAO;
import com.chatbot.model.Conversacion;
import com.chatbot.model.Producto;
import com.chatbot.model.Respuesta;

import java.util.List;
import java.util.Random;

public class ChatbotService {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final RespuestaDAO respuestaDAO = new RespuestaDAO();
    private final MensajeDAO mensajeDAO = new MensajeDAO();
    private final ConversacionDAO conversacionDAO = new ConversacionDAO();
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

        /*
         SI NO ENCUENTRA PRODUCTO,
         USA EL ÚLTIMO PRODUCTO DE LA CONVERSACIÓN
        */
        if (producto == null) {

            Conversacion conversacion =
                    conversacionDAO.obtener("cliente1");

            if (conversacion != null &&
                    conversacion.getUltimoProductoId() > 0) {

                producto = productoDAO.buscarPorId(
                        conversacion.getUltimoProductoId()
                );
            }
        }

        /*
         SI ENCUENTRA PRODUCTO
        */
        if (producto != null) {

            String respuesta;

            switch (intencion) {

                case "PRECIO":

                    respuesta = """
                    %s

                    El producto 🛒 %s

                    tiene un precio de 💰 S/ %.2f

                    📦 Actualmente tenemos %d unidades disponibles.
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

                    📦 Stock actual: %d unidades.
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

                case "ENVIO":

                    respuesta = """
                    %s

                    Sí 🚚 realizamos envíos para:

                    🛒 %s

                    💰 Precio: S/ %.2f

                    ¿Deseas coordinar entrega o más información? 😄
                    """.formatted(
                            saludoAleatorio(),
                            producto.getNombre(),
                            producto.getPrecio()
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

                    break;
            }

            /*
             GUARDA CONTEXTO DE CONVERSACIÓN
            */
            conversacionDAO.guardarContexto(
                    "cliente1",
                    producto.getId(),
                    intencion
            );

            /*
             GUARDA MENSAJE
            */
            mensajeDAO.guardar(
                    mensaje,
                    respuesta
            );

            return respuesta;
        }

        /*
         RESPUESTAS PREDEFINIDAS
        */
        List<Respuesta> respuestas =
                respuestaDAO.listarActivos();

        for (Respuesta r : respuestas) {

            String[] palabras =
                    r.getPalabrasClave().split(",");

            for (String palabra : palabras) {

                if (texto.contains(
                        palabra.trim().toLowerCase()
                )) {

                    mensajeDAO.guardar(
                            mensaje,
                            r.getRespuesta()
                    );

                    return r.getRespuesta();
                }
            }
        }

        /*
         IA OPENAI
        */
        String respuestaIA = openAIService.preguntar(
                """
                Cliente escribió:
                "%s"

                Productos disponibles:
                %s

                Responde como un vendedor amable, natural y profesional.
                Recomienda productos adecuados según lo que busca el cliente.
                """.formatted(
                        mensaje,
                        obtenerProductosTexto()
                )
        );

        mensajeDAO.guardar(
                mensaje,
                respuestaIA
        );

        return respuestaIA;
    }

    /*
     CONVIERTE PRODUCTOS A TEXTO PARA IA
    */
    private String obtenerProductosTexto() {

        StringBuilder sb = new StringBuilder();

        List<Producto> productos =
                productoDAO.listarActivos();

        for (Producto p : productos) {

            sb.append("""
                    Producto: %s
                    Categoría: %s
                    Marca: %s
                    Tags: %s
                    Precio: %s
                    Stock: %d
                    Descripción: %s

                    """.formatted(
                    p.getNombre(),
                    p.getCategoria(),
                    p.getMarca(),
                    p.getTags(),
                    p.getPrecio(),
                    p.getStock(),
                    p.getDescripcion()
            ));
        }

        return sb.toString();
    }

    /*
     DETECTAR INTENCIÓN
    */
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