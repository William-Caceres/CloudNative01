import { useIsAuthenticated } from "@azure/msal-react"
import { useEffect, useState } from "react"
import useAuthToken from "../hooks/useAuthToken"
import { deleteSMT, getSMT_ID } from "../utils/apiHelper"
import { useNavigate } from "react-router-dom"


function Tarjeta_reservas({id}){

    const [reserva, setReserva] = useState({})
    const [servicio, setServicio] = useState({})
    
    const isAuthenticated = useIsAuthenticated()
    const {token, loading} = useAuthToken()

    const navegar = useNavigate()

    const setId = (id) => {
        localStorage.setItem("ID_S_reserva",id)
        navegar("/mis_reservas/detalle")
    }
    const eliminarReserva = async(id) => {
        const res = await deleteSMT("8081","api/v1/reserva","delete",id,token)
        if(res){window.location.reload}
        console.log("Reserva eliminada?: ",res)
    }

    useEffect(()=>{

        if (loading){return}
        if (!token){return}

        const obtenerReservaID = async () => {
            try {
                const res = await getSMT_ID("8081","api/v1/reserva","get", id, token)
                setReserva(res)               
                
                
                const obtenerServicioID = async () => {
                    try {
                        const res_s = await getSMT_ID("8082","api/v1/servicio","get", res.id_servicio, token)
                        setServicio(res_s)               
                    }catch(error){
                        console.error("ERROR: ",error) 
                    }
                }
                obtenerServicioID()
            }catch(error){
                console.error("ERROR: ",error) 
            }
        }
        obtenerReservaID()

    },[token, loading])
    return(
        <>
        <div className="col-xl-3 col-lg-4 col-md-6 col-sm-12 col-xs-12">
            <div className="div_style p-4">
                <h3>{servicio.nombre}</h3>
                <p>{servicio.descripcion}</p>
                <p className="mb-1"><strong>Servicio reservado por: </strong>{reserva.id_usuario}</p>
                <p className="mb-1"><strong>Fecha reserva: </strong>{reserva.f_reserva}</p>
                <p className="mb-1"><strong>Fecha termino: </strong>{reserva.f_termino}</p>
                {isAuthenticated && 
                    <>
                    <button className="mt-3" onClick={()=>setId(reserva.id)}>Ver detalles</button>
                    <button onClick={()=>eliminarReserva(reserva.id)}>Cancelar reserva</button>
                    </>
                }
            </div>
        </div>
        </>
    )
}
export default Tarjeta_reservas