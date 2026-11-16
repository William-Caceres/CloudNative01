import { useNavigate } from 'react-router-dom'

function Inicio() {

    const navegar = useNavigate()

    return(
        <section className="gh-hero">
            <div>
                <p className="gh-eyebrow">Grand Hotel Group · 2026</p>
                <h1>Bienvenido a Grand Hotel Group. El detalle es la forma más silenciosa del lujo.</h1>
                <p>Una experiencia hotelera pensada para huéspedes que valoran el detalle, la calma y un servicio impecable.</p>
                <div className="gh-hero-actions">
                    <button className="gh-btn gh-btn-light" onClick={()=>navegar('servicios')}>Servicios</button>
                    <button className="gh-btn gh-btn-outline-light" onClick={()=>navegar('mis_reservas')}>Mis reservas</button>
                </div>
            </div>
            <aside className="gh-hero-aside">
                <p className="gh-eyebrow">Concierge</p>
                <strong>+56 2 2345 6789</strong>
                <div className="gh-divider"></div>
                <p>Disponible todos los días, a cualquier hora.</p>
            </aside>
        </section>
    )
}

export default Inicio
