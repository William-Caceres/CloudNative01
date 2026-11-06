import { useEffect, useState } from 'react'
import Formulario_servicios from '../components/Formulario_servicios'
import Tarjeta_servicios from '../components/Tarjeta_servicios'
import { listarServicios } from '../services/servicioService'

function Panel_admin() {

    const [showFormulario, setShowFormulario] = useState(false)
    const [servicios, setServicios] = useState([])
    const [error, setError] = useState(null)

    useEffect(()=>{
        const cargarServicios = async() => {
            try {
                setServicios(await listarServicios())
            } catch (e) {
                setError(e?.response?.data?.message || e.message)
            }
        }
        cargarServicios()
    },[showFormulario])

    return(
        <section className="gh-page">
            <header className="gh-page-head">
                <div>
                    <p className="gh-eyebrow">Administración</p>
                    <h1>Panel admin</h1>
                    <p>Gestione el catálogo de servicios del hotel.</p>
                </div>
                <button className="gh-btn gh-btn-primary" onClick={()=>setShowFormulario(true)}>Registrar servicio</button>
            </header>
            {error && <p className="gh-error">No se pudieron cargar los servicios: {error}</p>}
            <div className="gh-section-head">
                <h2>Servicios publicados</h2>
                <span className="gh-badge gh-badge-neutral">{servicios.length} activos</span>
            </div>
            <div className="gh-grid">
                {servicios.map((s)=>(
                    <Tarjeta_servicios servicio={s} key={s.id} admin/>
                ))}
            </div>
            {showFormulario && <Formulario_servicios show={setShowFormulario}/>}
        </section>
    )
}

export default Panel_admin
