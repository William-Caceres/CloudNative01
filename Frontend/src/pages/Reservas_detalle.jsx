import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { obtenerDashboard } from '../services/bffService'

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
        <article className="div_style">
            <h3>Detalle de reserva</h3>
            {error && <p className="text-danger">{error}</p>}
            {reserva && (
                <>
                <p><strong>N° reserva: </strong>{reserva.id}</p>
                <p><strong>Desde: </strong>{reserva.fechaReserva}</p>
                <p><strong>Hasta: </strong>{reserva.fechaTermino}</p>
                <p><strong>Tipo: </strong>{reserva.tipoReserva}</p>
                <p><strong>Personas: </strong>{reserva.cantidadPersonas}</p>
                <p className="mb-1"><strong>Valor final: </strong>{reserva.valorFinal}</p>
                </>
            )}
            <button className="mt-3" onClick={()=>navegar("/mis_reservas")}>Volver</button>
        </article>
    )
}

export default Reservas_detalle
