import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { obtenerDashboard } from '../services/bffService'

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
        <>
        <h1>Mis Reservas</h1>
        {cargando && <p>Cargando reservas...</p>}
        {error && <p className="text-danger">No se pudieron cargar las reservas: {error}</p>}
        {!cargando && !error && reservas.length === 0 && <p>No tienes reservas registradas.</p>}
        <section className="row m-0">
            {reservas.map((r)=>(
                <article className="col-xl-3 col-lg-4 col-md-6 col-sm-12 col-xs-12" key={r.id}>
                    <section className="div_style p-4">
                        <h3>Reserva #{r.id}</h3>
                        <p className="mb-0"><strong>Desde: </strong>{r.fechaReserva}</p>
                        <p className="mb-0"><strong>Hasta: </strong>{r.fechaTermino}</p>
                        <p className="mb-0"><strong>Tipo: </strong>{r.tipoReserva}</p>
                        <p className="mb-0"><strong>Personas: </strong>{r.cantidadPersonas}</p>
                        <p className="mb-1"><strong>Valor: </strong>{r.valorFinal}</p>
                        <button className="mt-3" onClick={()=>verDetalle(r.id)}>Ver detalle</button>
                    </section>
                </article>
            ))}
        </section>
        </>
    )
}

export default Reservas
