import { useState } from "react"

function Formulario_servicios(){

const [t_servicio, setT_servicio]           = useState("")
    const [precio, setPrecio]               = useState(0)
    const [n_habitacion, setN_habitacion]   = useState(0)
    const [capacidad, setCapacidad]         = useState(0)
    const [disponible, setDisponible]       = useState(true)
    const [nivel_s, setNivel_s]             = useState("")

    return(
        <>
        <div className="div_style">
            <form action="">
                <p>Tipo de servicio:</p>
                <select value={t_servicio} onChange={(e)=>setT_servicio(e.target.value)}>
                    <option value="" disabled defaultValue={""} hidden>Seleccione tipo de servicio</option>
                    <option value="habitacion">Habitacion</option>
                    <option value="comida">Comida</option>
                    <option value="recreativo">Recreativo</option>
                </select>
                <p>Precio:</p>
                <input 
                    className="NONE" type="number" required 
                    minLength={1} value={precio} 
                    onChange={(e)=>setPrecio(e.target.value)} 
                />
                <p>Numero de servicio o habitacion:</p>
                <input 
                    className="NONE" type="number" required 
                    minLength={1} value={n_habitacion} 
                    onChange={(e)=>setN_habitacion(e.target.value)} 
                />
                <p>Capacidad servicio:</p>
                <input 
                    className="NONE" type="number" required 
                    minLength={1} value={capacidad} 
                    onChange={(e)=>setCapacidad(e.target.value)} 
                />
                <p>Esta disponible?</p>
                <label>
                    <input 
                    type="checkbox" checked={disponible} 
                    onChange={(e) => setDisponible(e.target.checked)} />
                </label>
                <p>Nivel del servicio:</p>
                <select value={nivel_s} onChange={(e)=>setNivel_s(e.target.value)}>
                    <option value="" disabled defaultValue={""} hidden>Seleccione nivel de servicio</option>
                    <option value="1">Invitado</option>
                    <option value="2">Miembro</option>
                    <option value="3">VIP</option>
                </select>
            </form>
        </div>
        </>
    )
}

export default Formulario_servicios