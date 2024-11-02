package ar.edu.utn.dds.k3003.utils;

import ar.edu.utn.dds.k3003.model.DTO.SuscripcionDTO;
import ar.edu.utn.dds.k3003.model.Incidente;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


/***
 *
 * Clase que se encarga de creat tickets de fallas y enviar notificaciones
 * a los colaboradores necesarios haciendo uso de peticiones http
 * 
 * **/
public class utilsNotifIncidentAndEvents {

    public static void notificarFallaEnHeladeraTopic(Incidente incidente) {
        System.out.println("\n notification:\n" + incidente +"\n");
    }

    public static void notificarArregloDeHeladeraEnTopic(Integer heladeraId) {
        System.out.println("\n Heladera Arreglada: " + heladeraId+"\n");
    }

    //#TODO crear aviso de fraude
    public static void notificarFraudeHeladeraEnTopic(Incidente incidente) {
        System.out.println("\n notification: \n"+ incidente+"\n");
    }

    //#TODO crear aviso de temperatura
    public static void notificarExcesoTiempoTemperaturaMaximaEnTopic(Incidente incidente) {
        System.out.println("\n notification: \n"+ incidente + "\n");
    }

    //#TODO crear aviso de heladera abierta
    public static void notificarFallaEnConexionEnTopic(Incidente incidente) {
        System.out.println("\n notification: \n"+ incidente+"\n");
    }

    /**
     * Notifica a los colaboradores informandoles que quedan igual o menos cantidad de lo que setearon, o que ocurrio una falla en la heladera
     * **/
    public static void notificarAColaboradorDeSuSuscripcion(SuscripcionDTO suscripcion) {
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("URL_NOTIFICACIONES_COLABORADOR");

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper(); // Para convertir el DTO a JSON

        try {
            String json = objectMapper.writeValueAsString(suscripcion); // Convertir a JSON

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() != 200) {
                            throw new RuntimeException("Error en la respuesta: " + response.statusCode());
                        }
                        System.out.println("Notificación enviada correctamente a colaborador: " + suscripcion.colaboradorId);
                    })
                    .exceptionally(e -> {
                        throw new RuntimeException("Error durante la llamada a la API", e);
                    });

            System.out.println("Sending notification: " + suscripcion);
        } catch (Exception e) {
            e.printStackTrace(); // Manejo de excepciones
        }
    }
}
