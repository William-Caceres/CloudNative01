import { useEffect, useState } from 'react'
import { obtenerServicio } from '../services/servicioService'
import { useNavigate } from 'react-router-dom'

function formatearPrecio(valor) {
    return new Intl.NumberFormat('es-CL').format(valor)
}

function Servicios_detalle() {

    const [servicio, setServicio] = useState({})
    const navegar = useNavigate()

    useEffect(()=>{
        const cargarServicio = async() => {
            try {
                setServicio(await obtenerServicio(localStorage.getItem("ID_S")))
            } catch {
                alert("No se pudo cargar el servicio")
                navegar("/servicios")
            }
        }
        cargarServicio()
    },[navegar])

    return(
        <section className="gh-page">
            <div className="gh-page-head">
                <div>
                    <p className="gh-eyebrow">Detalle de servicio</p>
                    <h1>{servicio.nombre}</h1>
                </div>
                <span className={`gh-badge ${servicio.disponible ? '' : 'gh-badge-neutral'}`}>
                    {servicio.disponible ? 'Disponible' : 'No disponible'}
                </span>
            </div>
            <article className="gh-card">
                <p>{servicio.descripcion}</p>
                <p>{servicio.disponible ? '¡Está disponible!' : 'No disponible...'}</p>
                <div className="gh-detail-list">
                    <div className="gh-detail-row"><span>N° servicio</span><strong>{servicio.numHabitacion}</strong></div>
                    <div className="gh-detail-row"><span>Tipo</span><strong>{servicio.tipoServicio}</strong></div>
                    <div className="gh-detail-row"><span>Nivel</span><strong>{servicio.nivelServicio}</strong></div>
                    <div className="gh-detail-row"><span>Precio</span><strong>${formatearPrecio(servicio.precio)}</strong></div>
                    <div className="gh-detail-row"><span>Capacidad</span><strong>{servicio.capacidad} persona(s)</strong></div>
                </div>
                <div className="gh-card-actions">
                    <button className="gh-btn gh-btn-secondary" onClick={()=>navegar("/servicios")}>Volver</button>
                </div>
            </article>
        </section>
    )
}

export default Servicios_detalle
