import { Component } from 'react'

class ErrorBoundary extends Component {
    constructor(props) {
        super(props)
        this.state = { error: null }
    }

    static getDerivedStateFromError(error) {
        return { error }
    }

    componentDidCatch(error, info) {
        console.error('Error de render no controlado', error, info)
    }

    render() {
        if (this.state.error) {
            return (
                <section className="gh-page">
                    <header className="gh-page-head">
                        <div>
                            <p className="gh-eyebrow">Sistema</p>
                            <h1>Algo salió mal</h1>
                            <p>La aplicación encontró un error inesperado. Puede recargar la página para continuar.</p>
                        </div>
                    </header>
                    <p className="gh-error">No se pudo mostrar esta sección.</p>
                    <button className="gh-btn gh-btn-primary" type="button" onClick={() => window.location.reload()}>
                        Recargar
                    </button>
                </section>
            )
        }
        return this.props.children
    }
}

export default ErrorBoundary
