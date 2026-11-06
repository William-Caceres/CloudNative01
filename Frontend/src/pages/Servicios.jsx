import { useEffect, useState } from 'react'
import Tarjeta_servicios from '../components/Tarjeta_servicios'
import { listarServicios } from '../services/servicioService'

function Servicios() {

    const [servicios, setServicios] = useState([])
    const [error, setError] = useState(null)
    const [cargando, setCargando] = useState(true)

    useEffect(()=>{
        const cargarServicios = async() => {
            try {
                setServicios(await listarServicios())
            } catch (e) {
                setError(e?.response?.data?.message || e.message)
            } finally {
                setCargando(false)
            }
        }
        cargarServicios()
    },[])

    return(
        <>
        {cargando && <p>Cargando servicios...</p>}
        {error && <p className="text-danger">No se pudieron cargar los servicios: {error}</p>}
        {!cargando && !error && servicios.length === 0 && <p>No hay servicios disponibles.</p>}
        <section className="row m-0">
            {servicios.map((s)=>(
                <Tarjeta_servicios servicio={s} key={s.id}/>
            ))}
        </section>
        </>
    )
}

export default Servicios
