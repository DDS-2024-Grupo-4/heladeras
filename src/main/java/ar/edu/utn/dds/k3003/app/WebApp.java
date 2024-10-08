package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.controller.HeladeraController;
import ar.edu.utn.dds.k3003.clients.ViandasProxy;
import ar.edu.utn.dds.k3003.facades.dtos.Constants;
import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;

public class WebApp {

    public static EntityManagerFactory entityManagerFactory;
    public static Channel channel;
    public static Javalin app;

    public static void main(String[] args) throws IOException, TimeoutException {

        startEntityManagerFactory();
        Dotenv dotenv = Dotenv.load();
        var objectMapper = createObjectMapper();
        var fachada = new Fachada(entityManagerFactory);
        fachada.setViandasProxy(new ViandasProxy(objectMapper));
        var port = Integer.parseInt(dotenv.get("PORT"));

        app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
                configureObjectMapper(mapper);
            }));
        }).start(port);

        var heladeraController = new HeladeraController(fachada);

        app.get("/heladeras/crearGenericas", heladeraController::crearHeladerasGenericas);
        app.get("/heladeras/deleteAll", heladeraController::borrarTodo);
        app.post("/heladeras", heladeraController::agregar);
        app.get("/heladeras/{heladeraId}", heladeraController::obtenerHeladera);
        app.post("/depositos", heladeraController::depositarVianda);
        app.post("/retiros", heladeraController::retirarVianda);
        app.post("/temperaturasEnCola/registrar", heladeraController::registrarTemperaturaEnCola);
        app.get("/heladeras/{heladeraId}/temperaturas", heladeraController::obtenerTemperaturas);

        channel = initialCloudAMQPTopicConfiguration();
        setupConsumer(heladeraController);
    }

    public static ObjectMapper createObjectMapper() {
        var objectMapper = new ObjectMapper();
        configureObjectMapper(objectMapper);
        return objectMapper;
    }

    public static void configureObjectMapper(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var sdf = new SimpleDateFormat(Constants.DEFAULT_SERIALIZATION_FORMAT, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.setDateFormat(sdf);
    }

    public static void startEntityManagerFactory() {
        Map<String, Object> configOverrides = new HashMap<>();
        Dotenv dotenv = Dotenv.load();
        configOverrides.put("javax.persistence.jdbc.url", dotenv.get("jdbcUrl"));
        configOverrides.put("javax.persistence.jdbc.user", dotenv.get("jdbcUser"));
        configOverrides.put("javax.persistence.jdbc.password", dotenv.get("jdbcPassword"));
        configOverrides.put("javax.persistence.jdbc.driver", dotenv.get("jdbcDriver"));
        entityManagerFactory = Persistence.createEntityManagerFactory("heladeradb", configOverrides);
    }

    private static Channel initialCloudAMQPTopicConfiguration() throws IOException, TimeoutException {
        Dotenv dotenv = Dotenv.load();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(dotenv.get("QUEUE_HOST"));
        factory.setUsername(dotenv.get("QUEUE_USERNAME"));
        factory.setPassword(dotenv.get("QUEUE_PASSWORD"));
        factory.setVirtualHost(dotenv.get("VHOST"));
        Connection connection = factory.newConnection();
        return connection.createChannel();
    }

    private static void setupConsumer(HeladeraController heladeraController) throws IOException {
        Dotenv dotenv = Dotenv.load();
        String QUEUE = dotenv.get("QUEUE_NAME");
        channel.queueDeclare(QUEUE, false, false, false, null);
        System.out.println("Esperando mensajes en la cola " + QUEUE);

        // PROCESAMIENTO DE LOS MENSAJES
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");

            String[] parts = message.split(" - ");
            if (parts.length == 2) {
                String heladeraPart = parts[0]; // "Heladera X"
                String temperaturaPart = parts[1]; // "Temperatura Y°C"

                // Extraigo los valores de los mensajes
                int heladeraId = Integer.parseInt(heladeraPart.split(" ")[1]);
                Integer temperatura = Integer.parseInt(temperaturaPart.split(" ")[1].replace("°C", ""));

                TemperaturaDTO temperaturaDTO = new TemperaturaDTO(temperatura, heladeraId, LocalDateTime.now());

                System.out.println("Processed TemperaturaDTO: " + temperaturaDTO);
                heladeraController.registrarTemperatura(temperaturaDTO);
            } else {
                System.err.println("Formato de mensaje incorrecto: " + message);
            }

        };
        channel.basicConsume(QUEUE, true, deliverCallback, consumerTag -> { });
    }

}