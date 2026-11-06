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
        <section className="gh-page">
            <header className="gh-page-head">
                <div>
                    <p className="gh-eyebrow">Catálogo seleccionado</p>
                    <h1>Servicios</h1>
                    <p>Experiencias pensadas para cada momento de la estancia.</p>
                </div>
                <span className="gh-badge gh-badge-neutral">{servicios.length} disponibles</span>
            </header>
            {cargando && <p className="gh-loading">Cargando servicios...</p>}
            {error && <p className="gh-error">No se pudieron cargar los servicios: {error}</p>}
            {!cargando && !error && servicios.length === 0 && <p className="gh-empty">No hay servicios disponibles.</p>}
            <div className="gh-grid">
                {servicios.map((s)=>(
                    <Tarjeta_servicios servicio={s} key={s.id}/>
                ))}
            </div>
        </section>
    )
}

export default Servicios
