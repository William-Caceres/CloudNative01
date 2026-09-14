import { useEffect, useState } from "react"
import Tarjeta_servicios from "../components/Tarjeta_servicios"
import { getSMT } from "../utils/apiHelper"
import {useIsAuthenticated, useMsal} from "@azure/msal-react"
import useAuthToken from "../hooks/useAuthToken"

function Servicios() {

    const [servicios, setServicios] = useState([]) 
    const {token, loading, error} = useAuthToken()

    
    useEffect(()=>{

        if (loading){return}
        if (!token){return}

        const obtenerServicios = async () => {
            try{
                const res = await getSMT("8082","api/v1/servicio","list",token)
                setServicios(res)  
            }catch(error){
               console.error("ERROR: ",error) 
            }
        }
        obtenerServicios()

    }, [token, loading])
    
    return(
        <>
        
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

/*
const payload = JSON.parse(atob(token.split('.')[1]))
console.log("ISS: ", payload.iss)
console.log("AUD: ", payload.aud)
console.log("TID: ", payload.tid)
console.log("VER: ", payload.ver)
console.log("SCP: ", payload.scp)

const obtenerToken_listar = async () => {
        const account = accounts[0]

        const req = {
            scopes: ["api://0dffbac7-2223-48cb-85c8-ba2376e4e633/api_access"],
            account: account
        }
        try {
            const t_res = await instance.acquireTokenSilent(req)
            const token = t_res.accessToken
            const payload = JSON.parse(atob(token.split('.')[1]))
            console.log("ISS: ", payload.iss)
            console.log("AUD: ", payload.aud)
            console.log("TID: ", payload.tid)
            console.log("VER: ", payload.ver)
            console.log("SCP: ", payload.scp)
            console.log("ROL: ", payload.roles)
        
            const res = await getSMT("8082","api/v1/servicio","list",token)
            setServicios(res)   
        }catch(error){
            console.error("ERROR al recuperar el TOKEN: ",error)
        }
    }
*/