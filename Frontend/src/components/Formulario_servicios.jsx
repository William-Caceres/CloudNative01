import { useState } from "react"
import { postSMT } from "../utils/apiHelper"

function Formulario_servicios({show}){

    const [t_servicio, setT_servicio]       = useState("")
    const [precio, setPrecio]               = useState(0)
    const [n_habitacion, setN_habitacion]   = useState(0)
    const [capacidad, setCapacidad]         = useState(0)
    const [disponible, setDisponible]       = useState(true)
    const [nivel_s, setNivel_s]             = useState("")
    const [nombre, setNombre]               = useState("")
    const [desc, setDesc]                   = useState("")
    
    const registrarServicio = async() => {

        if(precio>0&&capacidad>0&&n_habitacion>0&&t_servicio!=""&&nivel_s!=""){
            const servicio = {
                tipoServicio: t_servicio,
                precio: precio,
                numHabitacion: n_habitacion,
                capacidad: capacidad,
                disponible: disponible,
                nivelServicio: nivel_s,
                nombre: nombre,
                descripcion: desc
            }
            postSMT("8082", "api/v1/servicio", "post", servicio)
            show(false)
        }else{
            alert("Valores numericos deben ser mayor a 0")
        }
    }
    const cerrarFormulario = () => {
        show(false)
    }

    return(
        <>
        <div className="card_black_bg">
        <div className="center_card m-0">
            <form action="" className="div_style form_card_size" onSubmit={()=>registrarServicio()}>
                <p className="mb-1 mt-3">ID de servicio o habitacion:</p>
                <input 
                    className="stuff_mwidth" type="number" required 
                    minLength={1} value={n_habitacion} 
                    onChange={(e)=>setN_habitacion(e.target.value)} 
                />
                <p className="mb-1 mt-3">Tipo de servicio:</p>
                <select value={t_servicio} onChange={(e)=>setT_servicio(e.target.value)}>
                    <option value="" disabled defaultValue={""} hidden>Seleccione tipo de servicio</option>
                    <option value="habitacion">Habitacion</option>
                    <option value="comida">Comida</option>
                    <option value="recreativo">Recreativo</option>
                </select>
                <p className="mb-1 mt-3">Nivel del servicio:</p>
                <select value={nivel_s} onChange={(e)=>setNivel_s(e.target.value)}>
                    <option value="" disabled defaultValue={""} hidden>Seleccione nivel de servicio</option>
                    <option value="invitado">Invitado</option>
                    <option value="miembro">Miembro</option>
                    <option value="VIP">VIP</option>
                </select>
                <p className="mb-1 mt-3">Nombre:</p>
                <input 
                    className="stuff_mwidth" type="text" required 
                    minLength={1} maxLength={50} value={nombre} 
                    onChange={(e)=>setNombre(e.target.value)} 
                />
                <p className="mb-1 mt-3">Descripcion:</p>
                <textarea 
                    className="stuff_mwidth" type="text" required 
                    minLength={1} maxLength={150} value={desc} 
                    onChange={(e)=>setDesc(e.target.value)} 
                />
                <p className="mb-1 mt-3">Precio:</p>
                <input 
                    className="stuff_mwidth" type="number" required 
                    minLength={1} value={precio} 
                    onChange={(e)=>setPrecio(e.target.value)} 
                />
                <p className="mb-1 mt-3">Capacidad servicio:</p>
                <input 
                    className="stuff_mwidth" type="number" required 
                    minLength={1} value={capacidad} 
                    onChange={(e)=>setCapacidad(e.target.value)} 
                />
                <p className="mb-1 mt-3">Esta disponible?</p>
                <label>
                    <input 
                    type="checkbox" checked={disponible} 
                    onChange={(e) => setDisponible(e.target.checked)} />
                </label>                
                <p className="mb-1 mt-3"></p>
                <button type="submit">Registrar</button>
                <button type="button" onClick={()=>cerrarFormulario()}>Cerrar</button>
            </form>
        </div>
        </div>
        </>
    )
}

export default Formulario_servicios