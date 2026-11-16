import { useEffect, useState } from 'react'
import { obtenerServicio, modificarServicio } from '../services/servicioService'

function Modificar_servicios({ show }) {

    const [t_servicio, setT_servicio]       = useState("")
    const [precio, setPrecio]               = useState(0)
    const [n_habitacion, setN_habitacion]   = useState(0)
    const [capacidad, setCapacidad]         = useState(0)
    const [disponible, setDisponible]       = useState(true)
    const [nivel_s, setNivel_s]             = useState("")
    const [nombre, setNombre]               = useState("")
    const [desc, setDesc]                   = useState("")

    const guardarModificacion = async() => {
        if(precio>0&&capacidad>0&&n_habitacion>=0&&t_servicio!==""&&nivel_s!==""){
            const id = localStorage.getItem("ID_S")
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
                await modificarServicio(id, servicio)
                show(false)
            } catch {
                alert("No se pudo modificar el servicio")
            }
        }else{
            alert("Precio y capacidad deben ser mayores a 0, y tipo/nivel de servicio son obligatorios")
        }
    }

    const cerrarFormulario = () => {
        show(false)
    }

    useEffect(()=>{
        const cargarServicio = async() => {
            try {
                const res = await obtenerServicio(localStorage.getItem("ID_S"))
                setT_servicio(res.tipoServicio)
                setPrecio(res.precio)
                setN_habitacion(res.numHabitacion)
                setCapacidad(res.capacidad)
                setDisponible(res.disponible)
                setNivel_s(res.nivelServicio)
                setNombre(res.nombre)
                setDesc(res.descripcion)
            } catch {
                alert("No se pudo cargar el servicio")
            }
        }
        cargarServicio()
    },[])

    return(
        <section className="gh-modal-backdrop">
            <form className="gh-modal" onSubmit={(e)=>{e.preventDefault(); guardarModificacion()}}>
                <p className="gh-eyebrow">Editar servicio</p>
                <h2>Modificar servicio</h2>
                <div className="gh-field">
                    <label htmlFor="n_habitacion">ID de servicio o habitación:</label>
                    <input id="n_habitacion" type="number" required value={n_habitacion} onChange={(e)=>setN_habitacion(e.target.value)} />
                </div>
                <div className="gh-field">
                    <label htmlFor="t_servicio">Tipo de servicio:</label>
                    <select id="t_servicio" value={t_servicio} onChange={(e)=>setT_servicio(e.target.value)}>
                        <option value="" disabled>Seleccione tipo de servicio</option>
                        <option value="habitacion">Habitación</option>
                        <option value="comida">Comida</option>
                        <option value="recreativo">Recreativo</option>
                    </select>
                </div>
                <div className="gh-field">
                    <label htmlFor="nivel_s">Nivel del servicio:</label>
                    <select id="nivel_s" value={nivel_s} onChange={(e)=>setNivel_s(e.target.value)}>
                        <option value="" disabled>Seleccione nivel de servicio</option>
                        <option value="invitado">Invitado</option>
                        <option value="miembro">Miembro</option>
                        <option value="VIP">VIP</option>
                    </select>
                </div>
                <div className="gh-field">
                    <label htmlFor="nombre">Nombre:</label>
                    <input id="nombre" type="text" required maxLength={50} value={nombre} onChange={(e)=>setNombre(e.target.value)} />
                </div>
                <div className="gh-field">
                    <label htmlFor="desc">Descripción:</label>
                    <textarea id="desc" required maxLength={150} value={desc} onChange={(e)=>setDesc(e.target.value)} />
                </div>
                <div className="gh-grid-tight">
                    <div className="gh-field">
                        <label htmlFor="precio">Precio:</label>
                        <input id="precio" type="number" required value={precio} onChange={(e)=>setPrecio(e.target.value)} />
                    </div>
                    <div className="gh-field">
                        <label htmlFor="capacidad">Capacidad servicio:</label>
                        <input id="capacidad" type="number" required value={capacidad} onChange={(e)=>setCapacidad(e.target.value)} />
                    </div>
                </div>
                <label className="gh-check">
                    <input id="disponible" type="checkbox" checked={disponible} onChange={(e)=>setDisponible(e.target.checked)} />
                    ¿Está disponible?
                </label>
                <div className="gh-form-actions">
                    <button className="gh-btn gh-btn-primary" type="submit">Modificar</button>
                    <button className="gh-btn gh-btn-secondary" type="button" onClick={cerrarFormulario}>Cerrar</button>
                </div>
            </form>
        </section>
    )
}

export default Modificar_servicios
