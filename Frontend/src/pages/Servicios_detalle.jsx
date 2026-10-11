import { useEffect, useState } from 'react'
import { obtenerServicio } from '../services/servicioService'
import { useNavigate } from 'react-router-dom'

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
        <article className="div_style">
            <h3>{servicio.nombre}</h3>
            <p>{servicio.descripcion}</p>
            <p><strong>N° servicio: </strong>{servicio.numHabitacion}</p>
            <p><strong>Tipo de servicio: </strong>{servicio.tipoServicio}</p>
            <p><strong>Nivel de servicio: </strong>{servicio.nivelServicio}</p>
            {servicio.disponible ? (
                <p>¡Está disponible!</p>
            ):(
                <p>No disponible...</p>
            )}
            <p className="mb-0"><strong>Precio:</strong>{servicio.precio}</p>
            <p className="mb-1"><strong>Capacidad:</strong>{servicio.capacidad} persona(s)</p>
            <button className="mt-3" onClick={()=>navegar("/servicios")}>Volver</button>
        </article>
    )
}

export default Servicios_detalle
