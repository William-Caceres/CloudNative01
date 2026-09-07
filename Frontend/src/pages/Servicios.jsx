import { useEffect, useState } from "react"
import Tarjeta_servicios from "../components/Tarjeta_servicios"
import Formulario_servicios from "../components/Formulario_servicios"
import { getSMT } from "../utils/apiHelper"

function Servicios() {

    const [showFormulario, setShowFormulario] = useState(false)
    const [servicios, setServicios] = useState([])    

    useEffect(()=>{
        const listarServicios = async() => {
            const res = await getSMT("8082","api/v1/servicio","list")
            setServicios(res)   
        }
        listarServicios()
    },[])
    
    return(
        <>
        <button onClick={()=>setShowFormulario(true)}>Registrar servicio</button>
        {showFormulario &&
            <Formulario_servicios show={setShowFormulario}/>
        }
        <div className="row m-0">
            {servicios.map((s,i)=>(
                <Tarjeta_servicios id={s.id} key={i}/>
            ))
            }
        </div>
        </>
    )
}

export default Servicios