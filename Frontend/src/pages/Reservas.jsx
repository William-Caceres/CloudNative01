import { useEffect, useState } from "react"
import useAuthToken from "../hooks/useAuthToken"
import Tarjeta_reservas from "../components/Tarjeta_reservas"
import { getSMT } from "../utils/apiHelper"

function Reservas() {

    const [reservas, setReservas] = useState([])
    const {token, loading} = useAuthToken()

    useEffect(()=>{
    
        if (loading){return}
        if (!token){return}

        const obtenerReservas = async () => {
            try{
                const res = await getSMT("8081","api/v1/reserva","list",token)
                setReservas(res)  
            }catch(error){
                console.error("ERROR: ",error) 
            }
        }
        obtenerReservas()

    }, [token, loading])
    return(
        <>
        <div className="row m-0">
           
            {reservas.map((s,i)=>(
                <Tarjeta_reservas id={s.id} key={i}/>
            ))
            }

        </div>
        </>
    )
}

export default Reservas