import { useEffect, useState } from "react"
import { getSMT_ID } from "../utils/apiHelper"
import { useNavigate } from "react-router-dom"

function Servicios_detalle() {

    const [servicio, setServicio] = useState({})
    const [id, setId] = useState(0)
    const navegar = useNavigate()

    useEffect(()=>{

        const obtenerServicio = async() => {
            const res = await getSMT_ID("8082","api/v1/servicio","get",localStorage.getItem("ID_S"))
            setServicio(res)
        }
        obtenerServicio()

    },[])

    return(
        <>
        <div className="div_style">
            <h3>{servicio.nombre}</h3>
            <p>{servicio.descripcion}</p>
            <p><strong>N° servicio: </strong>{servicio.numHabitacion}</p>
            <p><strong>Tipo de servicio: </strong>{servicio.tipoServicio}</p>
            <p><strong>Nivel de servicio: </strong>{servicio.nivelServicio}</p>
            {servicio.disponible ? (
                <p>Esta disponible!</p>
            ):(
                <p>No disponible...</p>
            )}
            <p className="mb-0"><strong>Precio:</strong>{servicio.precio}</p>
            <p className="mb-1"><strong>Capacidad:</strong>{servicio.capacidad} persona(s)</p>
            <button className="mt-3" onClick={()=>navegar("/servicios")}>Volver</button>
        </div>
        </>
    )
}

export default Servicios_detalle