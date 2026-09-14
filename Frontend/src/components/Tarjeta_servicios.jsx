import { useEffect, useState } from "react"
import { deleteSMT, getSMT_ID } from "../utils/apiHelper"
import { useLocation, useNavigate } from "react-router-dom"
import { useIsAuthenticated, useMsal } from "@azure/msal-react"
import Modificar_servicios from "./Modificar_servicios"
import useAuthToken from "../hooks/useAuthToken"
import Formulario_reservas from "./Formulario_reservas"

function Tarjeta_servicios({id}){

    const [showReserva, setShowReserva] = useState(false)

    const isAuthenticated = useIsAuthenticated()
    const location = useLocation()
    
    const [servicio, setServicio] = useState({})
    const [show, setShow] = useState(false)
    const [inAP, setInAP] = useState(false)
    const navegar = useNavigate()

    const {token, loading, error} = useAuthToken()

    const setId = (id) => {
        localStorage.setItem("ID_S",id)
        navegar("/servicios/detalle")
    }
    const modServicio = (id) => {
        localStorage.setItem("ID_S",id)
        setShow(true)
    }
    const eliminarServicio = async (id) => {
        if(confirm("Desea eliminar este servicio del hotel?")){
            const res = await deleteSMT("8082","api/v1/servicio","delete", id, token)
            if(res){window.location.reload()}
        }
    }
    const hacerReserva = () => {
        localStorage.setItem("ID_S_reserva",id)
        setShowReserva(true)
    }

    useEffect(()=>{

        if(location.pathname=="/PanelAdministradores"){setInAP(true)}

        if (loading){return}
        if (!token){return}

        const obtenerServicioID = async () => {
            try {
                const res = await getSMT_ID("8082","api/v1/servicio","get", id, token)
                setServicio(res)               
            }catch(error){
                console.error("ERROR: ",error) 
            }
        }
        obtenerServicioID()

    },[token, loading])

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
                    <button className="mt-3" onClick={()=>hacerReserva()}>Hacer una reserva</button>
                    <p></p>
                    {showReserva &&
                        <>
                            <Formulario_reservas show={setShowReserva}/>
                        </>
                    }
                    {inAP &&
                        <>
                        <button className="mt-3" onClick={()=>modServicio(id)}>Modificar</button>
                        <button className="mt-3" onClick={()=>eliminarServicio(id)}>Eliminar</button>
                        </>

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