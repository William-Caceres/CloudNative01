package Hotel.rabbitadmin.service;

import Hotel.rabbitadmin.dto.ColaInfo;
import Hotel.rabbitadmin.exception.MensajeriaException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Operaciones de administracion de RabbitMQ encapsuladas: los controllers
 * llaman metodos de alto nivel y no conocen la libreria de RabbitMQ.
 */
@Service
public class RabbitAdminService {

    private final RabbitAdmin rabbitAdmin;

    public RabbitAdminService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public void ra_crearCola(String nombre) {
        try {
            rabbitAdmin.declareQueue(QueueBuilder.durable(nombre).build());
        } catch (AmqpException ex) {
            throw new MensajeriaException("MSJ-001", HttpStatus.CONFLICT,
                    "No se pudo crear la cola " + nombre);
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
                    "No se pudo crear el exchange " + nombre);
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
                    "No se pudo crear el binding, verifica que el exchange " + exchange + " exista");
        }
    }
}
