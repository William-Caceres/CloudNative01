package Hotel.reserva.config;

import Hotel.reserva.messaging.PublicacionObservable;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Converter JSON del productor, identico al de rabbit-admin para que los
 * consumidores entiendan los eventos. La topologia la declara rabbit-admin.
 */
@Configuration
public class MensajeriaConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // Publisher confirms + returns: el broker confirma y devuelve lo que no tiene destino
    @Bean
    public RabbitTemplateCustomizer observadorDePublicaciones(PublicacionObservable observador) {
        return template -> {
            template.setConfirmCallback(observador);
            template.setReturnsCallback(observador);
        };
    }
}
