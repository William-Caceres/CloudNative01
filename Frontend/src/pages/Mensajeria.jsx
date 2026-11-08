import { useEffect, useState } from 'react'
import { obtenerCola, obtenerResumenKafka } from '../services/brokerAdminService'

const COLAS = [
    { nombre: 'notificaciones.queue', etiqueta: 'Notificaciones' },
    { nombre: 'tickets.queue', etiqueta: 'Tickets' },
    { nombre: 'documentos.queue', etiqueta: 'Documentos' },
]

function Mensajeria() {
    const [resumen, setResumen] = useState(null)
    const [colas, setColas] = useState([])
    const [error, setError] = useState(null)
    const [actualizado, setActualizado] = useState(null)

    useEffect(() => {
        let activo = true

        const cargar = async () => {
            try {
                const [kafka, ...rabbit] = await Promise.all([
                    obtenerResumenKafka(),
                    ...COLAS.map((cola) => obtenerCola(cola.nombre)),
                ])
                if (!activo) return
                setResumen(kafka)
                setColas(rabbit.map((cola, indice) => ({ ...cola, etiqueta: COLAS[indice].etiqueta })))
                setError(null)
                setActualizado(new Date())
            } catch (e) {
                if (activo) setError(e?.response?.data?.message || e.message)
            }
        }

        cargar()
        const timer = setInterval(cargar, 5000)
        return () => {
            activo = false
            clearInterval(timer)
        }
    }, [])

    return (
        <section className="gh-page">
            <header className="gh-page-head">
                <div>
                    <p className="gh-eyebrow">Operación</p>
                    <h1>Mensajería</h1>
                    <p>Estado en vivo de RabbitMQ y Kafka, sin salir del panel.</p>
                </div>
                <span className="gh-badge gh-badge-neutral">
                    {actualizado ? `Actualizado ${actualizado.toLocaleTimeString()}` : 'Conectando'}
                </span>
            </header>

            {error && <p className="gh-error">No se pudo leer la mensajería: {error}</p>}

            <div className="gh-section-head">
                <h2>Kafka</h2>
                <span className="gh-badge">{resumen ? 'En línea' : 'Esperando datos'}</span>
            </div>
            <div className="gh-metric-grid">
                <Metrica etiqueta="Brokers" valor={resumen?.brokers} />
                <Metrica etiqueta="Topics" valor={resumen?.topics} />
                <Metrica etiqueta="Grupos" valor={resumen?.grupos} />
                <Metrica etiqueta="Lag total" valor={resumen?.lagTotal} alerta={Number(resumen?.lagTotal) > 0} />
                <Metrica etiqueta="Reservas procesadas" valor={resumen?.reservasProcesadas} />
                <Metrica etiqueta="Pagos procesados" valor={resumen?.pagosProcesados} />
            </div>

            {resumen?.detalleConsumidores?.length > 0 && (
                <div className="gh-card">
                    <span className="gh-tag">Procesamiento idempotente</span>
                    <div className="gh-detail-list">
                        {resumen.detalleConsumidores.map((consumidor) => (
                            <div className="gh-detail-row" key={consumidor.consumidor}>
                                <span>{consumidor.consumidor}</span>
                                <strong>
                                    {consumidor.reservasProcesadas} reservas · {consumidor.pagosProcesados} pagos
                                </strong>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {resumen?.detalleGrupos?.length > 0 && (
                <div className="gh-card">
                    <span className="gh-tag">Consumer groups</span>
                    <div className="gh-detail-list">
                        {resumen.detalleGrupos.map((grupo) => (
                            <div className="gh-detail-row" key={grupo.grupo}>
                                <span>{grupo.grupo}</span>
                                <strong>{grupo.estado} · {grupo.miembros} miembros · lag {grupo.lagTotal}</strong>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            <div className="gh-section-head">
                <h2>RabbitMQ</h2>
                <span className="gh-badge">{colas.length === COLAS.length ? 'En línea' : 'Esperando datos'}</span>
            </div>
            <div className="gh-grid">
                {colas.map((cola) => (
                    <article className="gh-card" key={cola.nombre}>
                        <span className="gh-tag">Cola</span>
                        <h2>{cola.etiqueta}</h2>
                        <div className="gh-detail-list">
                            <div className="gh-detail-row"><span>Nombre</span><strong>{cola.nombre}</strong></div>
                            <div className="gh-detail-row"><span>Mensajes</span><strong>{cola.mensajes ?? 0}</strong></div>
                            <div className="gh-detail-row"><span>Consumidores</span><strong>{cola.consumidores ?? 0}</strong></div>
                        </div>
                    </article>
                ))}
            </div>

            <p className="gh-empty">
                Las interfaces nativas de RabbitMQ y Kafka UI siguen siendo la fuente de verdad para mensajes, colas, offsets y DLT.
            </p>
        </section>
    )
}

function Metrica({ etiqueta, valor, alerta = false }) {
    return (
        <article className={`gh-metric ${alerta ? 'gh-metric-alerta' : ''}`}>
            <span>{etiqueta}</span>
            <strong>{valor ?? '—'}</strong>
        </article>
    )
}

export default Mensajeria
