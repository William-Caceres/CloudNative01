import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { obtenerDashboard } from '../services/bffService'
import { formatearPrecio } from '../utils/format'

function Reservas() {

    const [reservas, setReservas] = useState([])
    const [error, setError] = useState(null)
    const [cargando, setCargando] = useState(true)
    const navegar = useNavigate()

    useEffect(()=>{
        const cargarReservas = async() => {
            try {
                const dashboard = await obtenerDashboard()
                const campo = dashboard?.reservas
                if (campo?.error) throw new Error(campo.error)
                setReservas(Array.isArray(campo?.datos) ? campo.datos : [])
            } catch (e) {
                setError(e?.response?.data?.message || e.message)
            } finally {
                setCargando(false)
            }
        }
        cargarReservas()
    },[])

    const verDetalle = (id) => {
        localStorage.setItem("ID_R", id)
        navegar("/mis_reservas/detalle")
    }

    return(
        <section className="gh-page">
            <header className="gh-page-head">
                <div>
                    <p className="gh-eyebrow">Su estancia</p>
                    <h1>Mis Reservas</h1>
                    <p>Consulte el detalle y estado de sus reservas.</p>
                </div>
                <span className="gh-badge gh-badge-neutral">{reservas.length} registradas</span>
            </header>
            {cargando && <p className="gh-loading">Cargando reservas...</p>}
            {error && <p className="gh-error">No se pudieron cargar las reservas: {error}</p>}
            {!cargando && !error && reservas.length === 0 && <p className="gh-empty">No tienes reservas registradas.</p>}
            <div className="gh-grid">
                {reservas.map((r)=>(
                    <article className="gh-card" key={r.id}>
                        <div className="gh-card-footer">
                            <span className="gh-tag">Reserva</span>
                            <span className={`gh-badge ${r.estadoPago === 'CONFIRMADO' ? '' : 'gh-badge-pending'}`}>
                                {r.estadoPago || 'PENDIENTE'}
                            </span>
                        </div>
                        <h2>Reserva #{r.id}</h2>
                        <div className="gh-detail-list">
                            <div className="gh-detail-row"><span>Desde</span><strong>{r.fechaReserva}</strong></div>
                            <div className="gh-detail-row"><span>Hasta</span><strong>{r.fechaTermino}</strong></div>
                            <div className="gh-detail-row"><span>Tipo</span><strong>{r.tipoReserva}</strong></div>
                            <div className="gh-detail-row"><span>Personas</span><strong>{r.cantidadPersonas}</strong></div>
                            <div className="gh-detail-row"><span>Valor</span><strong>${formatearPrecio(r.valorFinal)}</strong></div>
                        </div>
                        <div className="gh-card-actions">
                            <button className="gh-btn gh-btn-secondary gh-btn-small" onClick={()=>verDetalle(r.id)}>Ver detalle</button>
                        </div>
                    </article>
                ))}
            </div>
        </section>
    )
}

export default Reservas
