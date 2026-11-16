package Hotel.brokeradmin.kafka.dto;

public record KafkaConsumidorResumen(
        String consumidor,
        long reservasProcesadas,
        long pagosProcesados,
        String ultimoEvento,
        String ultimoEventoEn) {
}
