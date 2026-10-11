package Hotel.reserva.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;

/** Unitaria: contadores del observador de publicaciones (confirms + returns). */
class PublicacionObservableTest {

    private final PublicacionObservable observador = new PublicacionObservable();

    @Test
    void confirmacionExitosaIncrementaContador() {
        observador.confirm(null, true, null);
        observador.confirm(null, true, null);
        assertEquals(2, observador.confirmadas());
        assertEquals(0, observador.rechazadas());
    }

    @Test
    void confirmacionRechazadaNoCuentaComoExitosa() {
        observador.confirm(null, false, "broker sin espacio");
        assertEquals(0, observador.confirmadas());
        assertEquals(1, observador.rechazadas());
    }

    @Test
    void mensajeDevueltoIncrementaContador() {
        Message mensaje = new Message("{}".getBytes(), new MessageProperties());
        observador.returnedMessage(new ReturnedMessage(mensaje, 312, "NO_ROUTE", "hotel.topic", "ruta.sin.cola"));
        assertEquals(1, observador.devueltas());
        assertEquals(0, observador.confirmadas());
    }
}
