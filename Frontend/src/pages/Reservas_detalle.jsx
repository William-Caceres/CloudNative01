import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { obtenerDashboard } from '../services/bffService'

function formatearPrecio(valor) {
    return new Intl.NumberFormat('es-CL').format(valor)
}

function Reservas_detalle() {

    const [reserva, setReserva] = useState(null)
    const [error, setError] = useState(null)
    const navegar = useNavigate()

    useEffect(()=>{
        const cargarReserva = async() => {
            const id = Number(localStorage.getItem("ID_R"))
            if (!id) {
                setError("No se indicó una reserva")
                return
            }
            try {
                const dashboard = await obtenerDashboard()
                const campo = dashboard?.reservas
                if (campo?.error) throw new Error(campo.error)
                const encontrada = (Array.isArray(campo?.datos) ? campo.datos : []).find((r)=>r.id === id)
                if (!encontrada) {
                    setError("No se encontró la reserva solicitada")
                    return
                }
                setReserva(encontrada)
            } catch (e) {
                setError(e?.response?.data?.message || e.message)
            }
        }
        cargarReserva()
    },[])

    return(
        <section className="gh-page">
            <header className="gh-page-head">
                <div>
                    <p className="gh-eyebrow">Estancia</p>
                    <h1>Detalle de reserva</h1>
                </div>
                {reserva && (
                    <span className={`gh-badge ${reserva.estadoPago === 'CONFIRMADO' ? '' : 'gh-badge-pending'}`}>
                        {reserva.estadoPago || 'PENDIENTE'}
                    </span>
                )}
            </header>
            {error && <p className="gh-error">{error}</p>}
            {reserva && (
                <article className="gh-card">
                    <div className="gh-detail-list">
                        <div className="gh-detail-row"><span>N° reserva</span><strong>{reserva.id}</strong></div>
                        <div className="gh-detail-row"><span>Desde</span><strong>{reserva.fechaReserva}</strong></div>
                        <div className="gh-detail-row"><span>Hasta</span><strong>{reserva.fechaTermino}</strong></div>
                        <div className="gh-detail-row"><span>Tipo</span><strong>{reserva.tipoReserva}</strong></div>
                        <div className="gh-detail-row"><span>Personas</span><strong>{reserva.cantidadPersonas}</strong></div>
                        <div className="gh-detail-row"><span>Valor final</span><strong>${formatearPrecio(reserva.valorFinal)}</strong></div>
                    </div>
                    <div className="gh-card-actions">
                        <button className="gh-btn gh-btn-secondary" onClick={()=>navegar("/mis_reservas")}>Volver</button>
                    </div>
                </article>
            )}
        </section>
    )
}

export default Reservas_detalle
