import { useState } from 'react'
import { registrarServicio } from '../services/servicioService'

function Formulario_servicios({show}){

    const [t_servicio, setT_servicio]       = useState("")
    const [precio, setPrecio]               = useState(0)
    const [n_habitacion, setN_habitacion]   = useState(0)
    const [capacidad, setCapacidad]         = useState(0)
    const [disponible, setDisponible]       = useState(true)
    const [nivel_s, setNivel_s]             = useState("")
    const [nombre, setNombre]               = useState("")
    const [desc, setDesc]                   = useState("")

    const guardarServicio = async() => {

        if(precio>0&&capacidad>0&&n_habitacion>=0&&t_servicio!==""&&nivel_s!==""){
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
            try {
                await registrarServicio(servicio)
                show(false)
            } catch {
                alert("No se pudo registrar el servicio")
            }
        }else{
            alert("Precio y capacidad deben ser mayores a 0, y tipo/nivel de servicio son obligatorios")
        }
    }

    const cerrarFormulario = () => {
        show(false)
    }

    return(
        <section className="card_black_bg">
            <form className="div_style form_card_size" onSubmit={(e)=>{e.preventDefault(); guardarServicio()}}>
                <label className="mb-1 mt-3 d-block" htmlFor="n_habitacion">ID de servicio o habitación:</label>
                <input
                    id="n_habitacion" className="stuff_mwidth" type="number" required
                    value={n_habitacion}
                    onChange={(e)=>setN_habitacion(e.target.value)}
                />
                <label className="mb-1 mt-3 d-block" htmlFor="t_servicio">Tipo de servicio:</label>
                <select id="t_servicio" className="stuff_mwidth" value={t_servicio} onChange={(e)=>setT_servicio(e.target.value)}>
                    <option value="" disabled>Seleccione tipo de servicio</option>
                    <option value="habitacion">Habitación</option>
                    <option value="comida">Comida</option>
                    <option value="recreativo">Recreativo</option>
                </select>
                <label className="mb-1 mt-3 d-block" htmlFor="nivel_s">Nivel del servicio:</label>
                <select id="nivel_s" className="stuff_mwidth" value={nivel_s} onChange={(e)=>setNivel_s(e.target.value)}>
                    <option value="" disabled>Seleccione nivel de servicio</option>
                    <option value="invitado">Invitado</option>
                    <option value="miembro">Miembro</option>
                    <option value="VIP">VIP</option>
                </select>
                <label className="mb-1 mt-3 d-block" htmlFor="nombre">Nombre:</label>
                <input
                    id="nombre" className="stuff_mwidth" type="text" required
                    maxLength={50} value={nombre}
                    onChange={(e)=>setNombre(e.target.value)}
                />
                <label className="mb-1 mt-3 d-block" htmlFor="desc">Descripción:</label>
                <textarea
                    id="desc" className="stuff_mwidth" required
                    maxLength={150} value={desc}
                    onChange={(e)=>setDesc(e.target.value)}
                />
                <label className="mb-1 mt-3 d-block" htmlFor="precio">Precio:</label>
                <input
                    id="precio" className="stuff_mwidth" type="number" required
                    value={precio}
                    onChange={(e)=>setPrecio(e.target.value)}
                />
                <label className="mb-1 mt-3 d-block" htmlFor="capacidad">Capacidad servicio:</label>
                <input
                    id="capacidad" className="stuff_mwidth" type="number" required
                    value={capacidad}
                    onChange={(e)=>setCapacidad(e.target.value)}
                />
                <label className="mb-1 mt-3 d-block" htmlFor="disponible">¿Está disponible?</label>
                <input
                    id="disponible" type="checkbox" checked={disponible}
                    onChange={(e)=>setDisponible(e.target.checked)}
                />
                <button type="submit">Registrar</button>
                <button type="button" onClick={cerrarFormulario}>Cerrar</button>
            </form>
        </section>
    )
}

export default Formulario_servicios
