import { useEffect, useState } from "react"
import { getSMT_ID, postSMT } from "../utils/apiHelper"
import useAuthToken from "../hooks/useAuthToken"

function Formulario_reservas({show}){

    const [servicio, setServicio]       = useState({})
    const [fecha_r, setFecha_r]         = useState("")
    const [fecha_t, setFecha_t]         = useState("")
    const [tipo_r, setTipo_r]           = useState("")
    const [valor_f, setValor_f]         = useState(0)
    const [id_usuario, setId_usuario]   = useState(0)
    const [id_servicio, setId_servicio] = useState(0)

    const {token, loading} = useAuthToken()
    
    const registarReserva = async() => {
            if(servicio.tipoServicio!="habitacion"){
                const reserva = {
                    f_reserva: "NO_APLICA",
                    f_termino: "NO_APLICA",
                    tipo_reserva: servicio.nivelServicio,
                    valor_final: servicio.precio,
                    id_usuario: id_usuario,
                    id_servicio: servicio.id
                }
                postSMT("8081","api/v1/reserva","post",reserva,token)
                alert("Reserva realizada!")
                show(false)
            }else{
                if(fecha_r!=""&&fecha_t!=""&&id_usuario>0){
                    const reserva = {
                        f_reserva: fecha_r,
                        f_termino: fecha_t,
                        tipo_reserva: servicio.nivelServicio,
                        valor_final: servicio.precio,
                        id_usuario: id_usuario,
                        id_servicio: servicio.id
                    }
                    postSMT("8081","api/v1/reserva","post",reserva,token)
                    alert("Reserva realizada!")
                    show(false)
                }else{
                    alert("Todos los campos deben tener algun valor")
                }
            }
        
    }

    const cerrarFormulario = () => {
        show(false)
    }

    useEffect(()=>{

        if (loading){return}
        if (!token){return}

        const obtenerServicioID = async () => {
            try {
                const res = await getSMT_ID("8082","api/v1/servicio","get", localStorage.getItem("ID_S_reserva"), token)
                setServicio(res)               
            }catch(error){
                console.error("ERROR: ",error) 
            }
        }
        obtenerServicioID()

    },[token, loading])

    return(
        <>
        <div className="card_black_bg">
        <div className="center_card m-0">
            <div  className="div_style form_card_size">
                <h3>{servicio.nombre}</h3>
                <p>{servicio.descripcion}</p>
                <p><strong>N° servicio: </strong>{servicio.numHabitacion}</p>
                <p><strong>Tipo de servicio: </strong>{servicio.tipoServicio}</p>
                <p><strong>Nivel de servicio: </strong>{servicio.nivelServicio}</p>

                {servicio.tipoServicio=="habitacion" &&
                    <>
                    <strong>Dia de la reserva:</strong><p></p>
                    <input type="date" value={fecha_r} onChange={(e)=>setFecha_r(e.target.value)}/>

                    <p></p><strong>Dia de termino:</strong><p></p>
                    <input type="date" value={fecha_t} onChange={(e)=>setFecha_t(e.target.value)}/>
                    </>
                }

                <p className="mb-1 mt-3">ID usuario:</p>
                <input 
                    className="stuff_mwidth" type="number" required 
                    minLength={1} maxLength={50} value={id_usuario} 
                    onChange={(e)=>setId_usuario(e.target.value)} 
                />
                <button onClick={()=>registarReserva()}>Hacer reserva</button>
                <button type="button" onClick={()=>cerrarFormulario()}>Cerrar</button>
            </div>
        </div>
        </div>
        </>
    )
}
export default Formulario_reservas
/*
    private String f_reserva;
    private String f_termino;
    private String tipo_reserva;
    private Integer valor_final;
    private Integer id_usuario;
    private Integer id_servicio;
*/