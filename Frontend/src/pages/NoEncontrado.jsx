import { Link } from 'react-router-dom'

function NoEncontrado() {
    return (
        <section className="gh-page">
            <header className="gh-page-head">
                <div>
                    <p className="gh-eyebrow">Navegación</p>
                    <h1>Página no encontrada</h1>
                    <p>La dirección solicitada no existe o ya no está disponible.</p>
                </div>
            </header>
            <Link className="gh-btn gh-btn-primary" to="/">Volver al inicio</Link>
        </section>
    )
}

export default NoEncontrado
