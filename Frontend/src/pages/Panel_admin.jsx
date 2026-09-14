import { useEffect, useState } from "react"
import Formulario_servicios from "../components/Formulario_servicios"
import Tarjeta_servicios from "../components/Tarjeta_servicios"
import { getSMT } from "../utils/apiHelper"
import useAuthToken from "../hooks/useAuthToken"
import { useNavigate } from "react-router-dom"
import Tarjeta_reservas from "../components/Tarjeta_reservas"

function Panel_admin() {

    const [showFormulario, setShowFormulario] = useState(false)
    const [servicios, setServicios] = useState([]) 
    const [reservas, setReservas] = useState([])

    const {token, loading, error} = useAuthToken()

    const navegar = useNavigate()
    
    useEffect(()=>{

        
        if (loading){return}
        if (!token){return}

        const payload = JSON.parse(atob(token.split(".")[1]))
        const tieneRolAdministrador = payload.roles?.includes("admin")

        if(!tieneRolAdministrador){
            navegar("/")
        }

        const obtenerServicios = async () => {
            try{
                const res = await getSMT("8082","api/v1/servicio","list",token)
                setServicios(res)  
            }catch(error){
               console.error("ERROR: ",error) 
            }
        }
        obtenerServicios()
        const obtenerReservas = async () => {
            try{
                const res = await getSMT("8081","api/v1/reserva","list",token)
                setReservas(res)  
            }catch(error){
                console.error("ERROR: ",error) 
            }
        }
        obtenerReservas()
        
    },[token, loading])

    return(
        <>
        <div>
            <h1>Panel admin</h1>
            <button onClick={()=>setShowFormulario(true)}>Registrar servicio</button>
            {showFormulario &&
                <Formulario_servicios show={setShowFormulario}/>
            }
        </div>
        <h3>Servicios registrados</h3>
        <div className="row m-0">
   
            {servicios.map((s,i)=>(
                <Tarjeta_servicios id={s.id} key={i}/>
            ))
            }

        </div>
        <h3>Reservas registradas</h3>
        <div className="row m-0">
            {reservas.map((r,i)=>(
                <Tarjeta_reservas id={r.id} key={i}/>
            ))
            }
        </div>
        </>
    )
}

export default Panel_admin