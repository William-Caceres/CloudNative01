package Hotel.brokeradmin.kafka.dto;

import java.util.List;

public record KafkaResumen(
        int brokers,
        int topics,
        int grupos,
        long lagTotal,
        long reservasProcesadas,
        long pagosProcesados,
        String ultimoEvento,
        String ultimoEventoEn,
        List<KafkaGrupoResumen> detalleGrupos,
        List<KafkaConsumidorResumen> detalleConsumidores) {
}
