package Hotel.rabbitadmin.service;

import Hotel.rabbitadmin.dto.ColaInfo;
import Hotel.rabbitadmin.exception.MensajeriaException;
import java.nio.charset.StandardCharsets;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Operaciones de RabbitMQ encapsuladas: los controllers no conocen la libreria.
 */
@Service
public class RabbitAdminService {

    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;

    public RabbitAdminService(RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void ra_crearCola(String nombre) {
        try {
            rabbitAdmin.declareQueue(QueueBuilder.durable(nombre).build());
        } catch (AmqpException ex) {
            throw new MensajeriaException("MSJ-001", HttpStatus.CONFLICT,
                    "No se pudo crear la cola " + nombre, ex);
        }
    }

    public ColaInfo ra_infoCola(String nombre) {
        QueueInformation info = rabbitAdmin.getQueueInfo(nombre);
        if (info == null) {
            throw new MensajeriaException("MSJ-002", HttpStatus.NOT_FOUND,
                    "No existe la cola: " + nombre);
        }
        return new ColaInfo(info.getName(), info.getMessageCount(), info.getConsumerCount());
    }

    public void ra_eliminarCola(String nombre) {
        ra_infoCola(nombre);
        rabbitAdmin.deleteQueue(nombre);
    }

    public int ra_purgarCola(String nombre) {
        ra_infoCola(nombre);
        return rabbitAdmin.purgeQueue(nombre);
    }

    public void ra_crearExchange(String nombre, String tipo) {
        Exchange exchange = "topic".equalsIgnoreCase(tipo)
                ? ExchangeBuilder.topicExchange(nombre).durable(true).build()
                : ExchangeBuilder.directExchange(nombre).durable(true).build();
        try {
            rabbitAdmin.declareExchange(exchange);
        } catch (AmqpException ex) {
            throw new MensajeriaException("MSJ-003", HttpStatus.CONFLICT,
                    "No se pudo crear el exchange " + nombre, ex);
        }
    }

    // El broker trata el delete como idempotente: si el exchange no existe igual responde OK
    public void ra_eliminarExchange(String nombre) {
        rabbitAdmin.deleteExchange(nombre);
    }

    public void ra_crearBinding(String cola, String exchange, String routingKey) {
        ra_infoCola(cola);
        try {
            rabbitAdmin.declareBinding(new Binding(cola, Binding.DestinationType.QUEUE, exchange, routingKey, null));
        } catch (AmqpException ex) {
            throw new MensajeriaException("MSJ-005", HttpStatus.NOT_FOUND,
                    "No se pudo crear el binding, verifica que el exchange " + exchange + " exista", ex);
        }
    }

    // Publica un mensaje de prueba (JSON crudo) en el exchange indicado, desde /publish.
    public void ra_publicar(String exchange, String routingKey, String payload) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setContentEncoding(StandardCharsets.UTF_8.name());
        Message mensaje = new Message(payload.getBytes(StandardCharsets.UTF_8), props);
        try {
            rabbitTemplate.send(exchange, routingKey, mensaje);
        } catch (AmqpException ex) {
            throw new MensajeriaException("MSJ-004", HttpStatus.BAD_REQUEST,
                    "No se pudo publicar, verifica que el exchange " + exchange + " exista", ex);
        }
    }
}
