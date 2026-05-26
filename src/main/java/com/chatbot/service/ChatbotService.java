package com.chatbot.service;

import com.chatbot.dao.ConversacionDAO;
import com.chatbot.dao.MensajeDAO;
import com.chatbot.dao.ProductoDAO;
import com.chatbot.dao.RespuestaDAO;
import com.chatbot.model.Conversacion;
import com.chatbot.model.Producto;
import com.chatbot.model.Respuesta;
import com.chatbot.model.PersonalidadChatbot;

import java.util.List;
import java.util.Random;

public class ChatbotService {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final RespuestaDAO respuestaDAO = new RespuestaDAO();
    private final MensajeDAO mensajeDAO = new MensajeDAO();
    private final ConversacionDAO conversacionDAO = new ConversacionDAO();
    private final OpenAIService openAIService = new OpenAIService();
    private final Random random = new Random();
    private final PersonalidadChatbot personalidad = PersonalidadChatbot.GAMER;

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

private String generarCierre() {

        String[] cierres = {
                "¿Te gustaría más información? 😊",
                "Puedo ayudarte con más detalles si deseas 🔥",
                "También puedo recomendarte productos similares 😄",
                "Si deseas, puedo mostrarte otras opciones disponibles 👌",
                "Tenemos más modelos disponibles 🚀"
        };

        return cierres[random.nextInt(cierres.length)];
}

private String aplicarPersonalidad(String mensaje) {

    boolean mensajeNegativo =
            mensaje.toLowerCase().contains("no contamos") ||
            mensaje.toLowerCase().contains("no disponible") ||
            mensaje.toLowerCase().contains("agotado");

    switch (personalidad) {

        case GAMER:

            if (mensajeNegativo) {

                return """
                🎮 %s
                """.formatted(mensaje);
            }

            return """
            🎮 %s

            🔥 Aprovecha antes que se agote.
            """.formatted(mensaje);

        case ELEGANTE:

            return """
            ✨ %s

            Será un gusto ayudarte con cualquier consulta adicional.
            """.formatted(mensaje);

        case MARKETPLACE:

            return """
            🛒 %s

            📩 Escríbenos para coordinar compra o entrega.
            """.formatted(mensaje);

        case SOPORTE:

            return """
            🛠️ %s

            Si necesitas especificaciones técnicas,
            puedo ayudarte.
            """.formatted(mensaje);

        case PREMIUM:

            return """
            👑 %s

            Producto altamente recomendado para una experiencia premium.
            """.formatted(mensaje);

        default:
            return mensaje;
    }
}

    public String procesarMensaje(String mensaje) {

        if (mensaje == null || mensaje.isBlank()) {
            return "Escribe un mensaje.";
        }

        String texto = mensaje.toLowerCase();

        String intencion = detectarIntencion(texto);

        /*
        SI EL MENSAJE NO BUSCA PRODUCTOS,
        RESPONDE CON IA NATURAL
        */
        if (!mensajeBuscaProducto(texto)) {

        String respuestaIA =
                openAIService.preguntar(
                        """
                        Responde como un vendedor gamer amable y natural.

                        Cliente:
                        "%s"
                        """.formatted(mensaje)
                );

        mensajeDAO.guardar(
                mensaje,
                respuestaIA
        );

        return respuestaIA;
        }

        Producto producto = productoDAO.buscarCoincidencia(texto);

        /*
         SI NO ENCUENTRA PRODUCTO,
         USA EL ÚLTIMO PRODUCTO DE LA CONVERSACIÓN
        */
        boolean usarContexto =
                texto.contains("cuesta") ||
                texto.contains("precio") ||
                texto.contains("stock") ||
                texto.contains("disponible") ||
                texto.contains("ese") ||
                texto.contains("esa") ||
                texto.contains("lo quiero") ||
                texto.contains("me interesa");

        if (producto == null && usarContexto) {
        Conversacion conversacion =
                conversacionDAO.obtener("cliente1");


        if (conversacion != null) {
                producto = productoDAO.buscarPorId(
                        conversacion.getUltimoProductoId()
                );
        }
        }

        if (quiereAlternativa(texto)) {
        Conversacion conv =
                conversacionDAO.obtener("cliente1");

        if (conv != null) {

                Producto alternativo =
                        productoDAO.buscarAlternativa(
                                conv.getUltimaCategoria(),
                                conv.getPreferenciaPrecio(),
                                conv.getUltimoProductoId()
                        );

                if (alternativo != null) {

                String respuesta = """
                😊 También podría interesarte esta opción:

                🛒 %s

                💰 Precio: S/ %.2f
                📦 Stock: %d unidades

                📄 %s
                """.formatted(
                        alternativo.getNombre(),
                        alternativo.getPrecio(),
                        alternativo.getStock(),
                        alternativo.getDescripcion()
                );

                return aplicarPersonalidad(respuesta);
                }
        }
        }

        if (buscaProductoInexistente(texto, producto)) {

        String respuesta = """
        😅 Actualmente no contamos con ese producto exacto.

        Pero sí tenemos otras opciones gamer disponibles 🎮

        ¿Te gustaría que te recomiende algo similar? 👌
        """;

        return aplicarPersonalidad(respuesta);
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
                        
                        El producto:
                        🖥️ %s
                        
                        tiene un precio actual de:
                        💰 S/ %.2f
                        
                        📦 Stock disponible: %d unidades
                        
                        %s
                        """.formatted(
                                saludoAleatorio(),
                                producto.getNombre(),
                                producto.getPrecio(),
                                producto.getStock(),
                                generarCierre()
                        );

                        break;

                case "STOCK":

                        respuesta = """
                        %s
                        
                        Sí 😊 tenemos disponible:
                        
                        🛒 %s
                        
                        📦 Stock actual: %d unidades
                        
                        💰 Precio: S/ %.2f
                        
                        %s
                        """.formatted(
                                saludoAleatorio(),
                                producto.getNombre(),
                                producto.getStock(),
                                producto.getPrecio(),
                                generarCierre()
                        );

                        break;

                case "COMPRA":

                        respuesta = """
                        %s
                        
                        Excelente elección 🔥
                        
                        🛒 Producto:
                        %s
                        
                        💰 Precio: S/ %.2f
                        
                        📦 Disponibles: %d unidades
                        
                        📄 %s
                        
                        %s
                        """.formatted(
                                saludoAleatorio(),
                                producto.getNombre(),
                                producto.getPrecio(),
                                producto.getStock(),
                                producto.getDescripcion(),
                                generarCierre()
                        );

                        break;

                case "ENVIO":

                        respuesta = """
                        %s
                        
                        🚚 Sí realizamos envíos.
                        
                        Producto:
                        🛒 %s
                        
                        💰 Precio: S/ %.2f
                        
                        📦 Stock disponible: %d
                        
                        Podemos coordinar entrega inmediata 😄
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
                        
                        Tenemos disponible este producto 👇
                        
                        🛒 %s
                        
                        💰 Precio: S/ %.2f
                        
                        📦 Stock: %d unidades
                        
                        📄 Descripción:
                        %s
                        
                        %s
                        """.formatted(
                                saludoAleatorio(),
                                producto.getNombre(),
                                producto.getPrecio(),
                                producto.getStock(),
                                producto.getDescripcion(),
                                generarCierre()
                        );

                        break;
                }

            /*
             GUARDA CONTEXTO DE CONVERSACIÓN
            */
        String preferencia = "NORMAL";

        if (
                texto.contains("barato") ||
                texto.contains("economico") ||
                texto.contains("económico")
        ) {
        preferencia = "BARATO";
        }

        if (
                texto.contains("premium") ||
                texto.contains("pro")
        ) {
        preferencia = "PREMIUM";
        }

        conversacionDAO.guardarContexto(
                "cliente1",
                producto.getId(),
                intencion,
                producto.getCategoria(),
                preferencia
        );

            /*
             GUARDA MENSAJE
            */
            mensajeDAO.guardar(
                    mensaje,
                    respuesta
            );

            return aplicarPersonalidad(respuesta);
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
        texto.contains("comprar")
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

        private String detectarPreferenciaPrecio(String texto) {

        texto = texto.toLowerCase();

        if (
                texto.contains("barato") ||
                texto.contains("económico") ||
                texto.contains("economico")
        ) {
                return "BARATO";
        }

        if (
                texto.contains("premium") ||
                texto.contains("alta gama") ||
                texto.contains("pro")
        ) {
                return "PREMIUM";
        }

        return "NORMAL";
        }

        private boolean quiereAlternativa(String texto) {

        texto = texto.toLowerCase();

        return
                texto.contains("otra opcion") ||
                texto.contains("otra opción") ||
                texto.contains("algo mas") ||
                texto.contains("algo más") ||
                texto.contains("alternativa") ||
                texto.contains("otro producto") ||
                texto.contains("recomiendame otro") ||
                texto.contains("recomiéndame otro");
        }

        private boolean buscaProductoInexistente(
                String texto,
                Producto producto
        ) {

        texto = texto.toLowerCase();

        String[] productosBuscados = {
                "laptop",
                "monitor",
                "teclado",
                "mouse",
                "silla",
                "pc",
                "procesador",
                "audifonos",
                "microfono",
                "camara"
        };

        for (String p : productosBuscados) {

                if (texto.contains(p)) {

                if (
                        producto == null ||
                        !producto.getNombre()
                                .toLowerCase()
                                .contains(p)
                ) {

                        return true;
                }
                }
        }

        return false;
        }

        private boolean mensajeBuscaProducto(String texto) {

        texto = texto.toLowerCase();

        return
                texto.contains("monitor") ||
                texto.contains("mouse") ||
                texto.contains("teclado") ||
                texto.contains("laptop") ||
                texto.contains("silla") ||
                texto.contains("gamer") ||
                texto.contains("auricular") ||
                texto.contains("microfono") ||
                texto.contains("micrófono");
        }

}