import { useEffect, useState } from 'react'
import { obtenerServicio, eliminarServicio } from '../services/servicioService'
import { useNavigate } from 'react-router-dom'
import Modificar_servicios from './Modificar_servicios'
import { formatearPrecio } from '../utils/format'

function Tarjeta_servicios({ servicio: inicial, admin }) {

    const [servicio, setServicio] = useState(inicial)
    const [show, setShow] = useState(true)
    const [modificando, setModificando] = useState(false)
    const [recargar, setRecargar] = useState(false)
    const navegar = useNavigate()

    const id = inicial.id

    const setId = () => {
        localStorage.setItem("ID_S", id)
        navegar("/servicios/detalle")
    }

    const modificar = () => {
        localStorage.setItem("ID_S", id)
        setModificando(true)
    }

    const eliminar = async () => {
        if (confirm(`¿Eliminar el servicio "${servicio.nombre}"?`)) {
            try {
                await eliminarServicio(id)
                setShow(false)
            } catch {
                alert("No se pudo eliminar el servicio")
            }
        }
    }

    useEffect(()=>{
        if (!recargar) return
        const cargarServicio = async() => {
            try {
                setServicio(await obtenerServicio(id))
            } catch {
                alert("No se pudo recargar el servicio")
            } finally {
                setRecargar(false)
            }
        }
        cargarServicio()
    },[recargar, id])

    const cerrarModificacion = (abierto) => {
        setModificando(abierto)
        if (!abierto) setRecargar(true)
    }

    if (!show) return null

    return(
        <>
        <article className="gh-card">
            <span className="gh-tag">{servicio.tipoServicio || 'Servicio'}</span>
            <h2>{servicio.nombre}</h2>
            <p>{servicio.descripcion}</p>
            <div className="gh-price">
                <strong>${formatearPrecio(servicio.precio)}</strong>
                <span>CLP</span>
            </div>
            <p className="mt-2">Capacidad: {servicio.capacidad} persona(s)</p>
            <div className="gh-card-actions">
                <button className="gh-btn gh-btn-secondary gh-btn-small" onClick={setId}>Ver detalles</button>
                {admin && <button className="gh-btn gh-btn-secondary gh-btn-small" onClick={modificar}>Modificar</button>}
                {admin && <button className="gh-btn gh-btn-danger gh-btn-small" onClick={eliminar}>Eliminar</button>}
            </div>
        </article>
        {modificando && <Modificar_servicios show={cerrarModificacion}/>}
        </>
    )
}

export default Tarjeta_servicios
