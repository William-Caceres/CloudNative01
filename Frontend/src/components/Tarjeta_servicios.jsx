import { useEffect, useState } from "react"
import { getSMT_ID } from "../utils/apiHelper"

function Tarjeta_servicios({id}){

    const [servicio, setServicio] = useState({})

    useEffect(()=>{

        const obtenerServicio = async() => {
            const res = await getSMT_ID("8082","servicio","get",id)
            setServicio(res)
        }
        obtenerServicio()

    },[])

    return(
        <>
        <div className="col-xl-3 col-lg-4 col-md-6 col-sm-12 col-xs-12">
            <div className="div_style p-4">
                <h2>Tarjeta Servicio</h2>
            </div>
        </div>
        </>
    )
}

export default Tarjeta_servicios