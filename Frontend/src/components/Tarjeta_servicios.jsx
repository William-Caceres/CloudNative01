import { useEffect, useState } from "react"
import { getSMT_ID } from "../utils/apiHelper"
import { useNavigate } from "react-router-dom"

function Tarjeta_servicios({id}){

    const [servicio, setServicio] = useState({})
    const [show, setShow] = useState(true)
    const navegar = useNavigate()

    const setId = (id) => {
        localStorage.setItem("ID_S",id)
        navegar("/servicios/detalle")
    }

    useEffect(()=>{

        const obtenerServicio = async() => {
            const res = await getSMT_ID("8082","api/v1/servicio","get",id)
            if (res == null){
                setShow(false)
            }else{
                setServicio(res)
            }
        }
        obtenerServicio()

    },[])

    return(
        <>
        <div className="col-xl-3 col-lg-4 col-md-6 col-sm-12 col-xs-12">
            <div className="div_style p-4">
                <h3>{servicio.nombre}</h3>
                <p>{servicio.descripcion}</p>
                <p className="mb-0"><strong>Precio: </strong>{servicio.precio}</p>
                <p className="mb-1"><strong>Capacidad: </strong>{servicio.capacidad} persona(s)</p>
                <button className="mt-3" onClick={()=>setId(id)}>Ver detalles</button>
            </div>
        </div>
        </>
    )
}

export default Tarjeta_servicios