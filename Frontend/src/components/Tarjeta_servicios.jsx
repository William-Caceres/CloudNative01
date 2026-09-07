import { useEffect, useState } from "react"
import { getSMT_ID } from "../utils/apiHelper"
import { useLocation, useNavigate } from "react-router-dom"
import { useIsAuthenticated } from "@azure/msal-react"
import Modificar_servicios from "./Modificar_servicios"

function Tarjeta_servicios({id}){

    const isAuthenticated = useIsAuthenticated()
    const location = useLocation()
    
    const [servicio, setServicio] = useState({})
    const [show, setShow] = useState(false)
    const [inAP, setInAP] = useState(false)
    const navegar = useNavigate()

    const setId = (id) => {
        localStorage.setItem("ID_S",id)
        navegar("/servicios/detalle")
    }
    const modServicio = (id) => {
        localStorage.setItem("ID_S",id)
        setShow(true)
    }

    useEffect(()=>{
        
        if(location.pathname=="/PanelAdministradores"){setInAP(true)}

        const obtenerServicio = async() => {
            const res = await getSMT_ID("8082","api/v1/servicio","get",id)
            if (res == null){
                console.log("empty")
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
                {isAuthenticated && 
                    <>
                    {inAP &&
                        <button className="mt-3" onClick={()=>modServicio(id)}>Modificar</button>
                    }
                    {show &&
                        <>
                        <Modificar_servicios show={setShow}/>
                        </>
                    }
                    </>
                }
            </div>
        </div>
        </>
    )
}

export default Tarjeta_servicios