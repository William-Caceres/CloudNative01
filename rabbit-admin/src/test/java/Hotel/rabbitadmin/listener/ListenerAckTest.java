package Hotel.rabbitadmin.listener;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import Hotel.rabbitadmin.listener.documento.DocumentoListener;
import Hotel.rabbitadmin.listener.documento.DocumentoProcessor;
import Hotel.rabbitadmin.listener.notificacion.NotificacionListener;
import Hotel.rabbitadmin.listener.notificacion.NotificacionProcessor;
import Hotel.rabbitadmin.listener.ticket.TicketListener;
import Hotel.rabbitadmin.listener.ticket.TicketProcessor;
import com.rabbitmq.client.Channel;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

/**
 * Contrato de confirmacion de los consumidores (3 rutas): exito -> ACK,
 * invalido -> NACK sin reencolar (DLQ directa), recuperable -> NACK reencolando
 * UNA vez y en la reentrega -> NACK sin reencolar (DLQ).
 */
@ExtendWith(MockitoExtension.class)
class ListenerAckTest {

    private static final String ESTADO_TRANSITORIO = "fallo transitorio";
    private static final String ESTADO_PERSISTENTE = "fallo persistente";
    private static final String MENSAJE_INVALIDO = "dato invalido";
    private static final String CAMPO_RESERVA_ID = "reservaId";
    private static final String CAMPO_SERVICIO_ID = "servicioId";

    @Mock
    private Channel channel;

    @Mock
    private NotificacionProcessor procesadorNotificacion;

    @Mock
    private TicketProcessor procesadorTicket;

    @Mock
    private DocumentoProcessor procesadorDocumento;

    private NotificacionListener notificacion;
    private TicketListener ticket;
    private DocumentoListener documento;

    @BeforeEach
    void armar() {
        notificacion = new NotificacionListener(procesadorNotificacion);
        ticket = new TicketListener(procesadorTicket);
        documento = new DocumentoListener(procesadorDocumento);
    }

    @Test
    void eventoValidoConfirmaConAck() throws Exception {
        notificacion.procesarNotificacion(Map.of(CAMPO_RESERVA_ID, 1), channel, mensaje(false), 7L);
        ticket.procesarTicket(Map.of(CAMPO_RESERVA_ID, 1), channel, mensaje(false), 8L);
        documento.procesarDocumento(Map.of(CAMPO_SERVICIO_ID, 2), channel, mensaje(false), 9L);

        verify(channel).basicAck(7L, false);
        verify(channel).basicAck(8L, false);
        verify(channel).basicAck(9L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void eventoInvalidoVaDirectoADlqSinReintento() throws Exception {
        doThrow(new MensajeInvalidoException(MENSAJE_INVALIDO)).when(procesadorNotificacion).procesar(anyMap());
        doThrow(new MensajeInvalidoException(MENSAJE_INVALIDO)).when(procesadorTicket).procesar(anyMap());
        doThrow(new MensajeInvalidoException(MENSAJE_INVALIDO)).when(procesadorDocumento).procesar(anyMap());

        notificacion.procesarNotificacion(Map.of("foo", "bar"), channel, mensaje(false), 7L);
        ticket.procesarTicket(Map.of("foo", "bar"), channel, mensaje(false), 8L);
        documento.procesarDocumento(Map.of("foo", "bar"), channel, mensaje(false), 9L);

        verify(channel).basicNack(7L, false, false);
        verify(channel).basicNack(8L, false, false);
        verify(channel).basicNack(9L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void falloRecuperablePrimeraEntregaReencolaUnaVez() throws Exception {
        doThrow(new IllegalStateException(ESTADO_TRANSITORIO)).when(procesadorNotificacion).procesar(anyMap());
        doThrow(new IllegalStateException(ESTADO_TRANSITORIO)).when(procesadorTicket).procesar(anyMap());
        doThrow(new IllegalStateException(ESTADO_TRANSITORIO)).when(procesadorDocumento).procesar(anyMap());

        notificacion.procesarNotificacion(Map.of(CAMPO_RESERVA_ID, 1), channel, mensaje(false), 7L);
        ticket.procesarTicket(Map.of(CAMPO_RESERVA_ID, 1), channel, mensaje(false), 8L);
        documento.procesarDocumento(Map.of(CAMPO_SERVICIO_ID, 2), channel, mensaje(false), 9L);

        verify(channel).basicNack(7L, false, true);
        verify(channel).basicNack(8L, false, true);
        verify(channel).basicNack(9L, false, true);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void falloRecuperableEnReentregaVaADlq() throws Exception {
        doThrow(new IllegalStateException(ESTADO_PERSISTENTE)).when(procesadorNotificacion).procesar(anyMap());
        doThrow(new IllegalStateException(ESTADO_PERSISTENTE)).when(procesadorTicket).procesar(anyMap());
        doThrow(new IllegalStateException(ESTADO_PERSISTENTE)).when(procesadorDocumento).procesar(anyMap());

        notificacion.procesarNotificacion(Map.of(CAMPO_RESERVA_ID, 1), channel, mensaje(true), 7L);
        ticket.procesarTicket(Map.of(CAMPO_RESERVA_ID, 1), channel, mensaje(true), 8L);
        documento.procesarDocumento(Map.of(CAMPO_SERVICIO_ID, 2), channel, mensaje(true), 9L);

        verify(channel).basicNack(7L, false, false);
        verify(channel).basicNack(8L, false, false);
        verify(channel).basicNack(9L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    private Message mensaje(boolean reentregado) {
        MessageProperties props = new MessageProperties();
        props.setRedelivered(reentregado);
        return new Message(new byte[0], props);
    }
}
