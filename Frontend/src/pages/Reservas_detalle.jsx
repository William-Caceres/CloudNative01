import { useIsAuthenticated } from "@azure/msal-react"
import { useEffect, useState } from "react"
import useAuthToken from "../hooks/useAuthToken"
import { useNavigate } from "react-router-dom"
import { getSMT_ID } from "../utils/apiHelper"

function Reservas_detalle() {

    const [reserva, setReserva] = useState({})
    const [servicio, setServicio] = useState({})
    
    const isAuthenticated = useIsAuthenticated()
    const {token, loading} = useAuthToken()

    const navegar = useNavigate()

    useEffect(()=>{

        if (loading){return}
        if (!token){return}

        const obtenerReservaID = async () => {
            try {
                const res = await getSMT_ID("8081","api/v1/reserva","get", localStorage.getItem("ID_S_reserva"), token)
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
        <div className="div_style">
            <h3>{servicio.nombre}</h3>
            <p>{servicio.descripcion}</p>
            <p><strong>N° servicio: </strong>{servicio.numHabitacion}</p>
            <p><strong>Tipo de servicio: </strong>{servicio.tipoServicio}</p>
            <p><strong>Nivel de servicio: </strong>{servicio.nivelServicio}</p>
            <p></p>
            <strong>Reservado por:</strong><p>{reserva.id_usuario}</p>
            <strong>Fecha reserva:</strong><p>{reserva.f_reserva}</p>
            <strong>Fecha termino:</strong><p>{reserva.f_termino}</p>
            <p></p>
            <strong>Valor reserva:</strong><p>{reserva.valor_final}</p>
            <button className="mt-3" onClick={()=>navegar("/mis_reservas")}>Volver</button>
        </div>
        </>
    )
}

export default Reservas_detalle