import { useEffect, useState } from 'react'
import { useMsal, useIsAuthenticated, useAccount } from '@azure/msal-react'
import { getRoles, getAccessTokenScopes } from '../auth/authUtils'
import { obtenerDashboard } from '../services/bffService'

function Mi_cuenta() {
    const { instance } = useMsal()
    const isAuthenticated = useIsAuthenticated()
    const accounts = instance.getAllAccounts()
    const account = useAccount(accounts.length > 0 ? accounts[0] : null)
    const [scopes, setScopes] = useState([])
    const [dashboard, setDashboard] = useState(null)
    const [errorDashboard, setErrorDashboard] = useState(null)

    const claims = account?.idTokenClaims || {}
    const roles = getRoles(account)

    useEffect(() => {
        if (!isAuthenticated) return
        getAccessTokenScopes(instance).then(setScopes).catch(() => setScopes([]))
        obtenerDashboard()
            .then(setDashboard)
            .catch((e) => setErrorDashboard(e?.response?.data?.message || e.message))
    }, [isAuthenticated, instance])

    if (!isAuthenticated) {
        return (
            <section className="gh-page">
                <header className="gh-page-head">
                    <div><p className="gh-eyebrow">Perfil</p><h1>Mi cuenta</h1></div>
                </header>
                <p className="gh-empty">Debes iniciar sesión para ver tu cuenta.</p>
            </section>
        )
    }

    return (
        <section className="gh-page">
            <header className="gh-page-head">
                <div>
                    <p className="gh-eyebrow">Perfil</p>
                    <h1>Mi cuenta</h1>
                    <p>Su identidad y el resumen de su actividad.</p>
                </div>
                <span className="gh-user-pill" style={{ color: 'var(--gh-ink)', borderColor: 'var(--gh-line)' }}>
                    {roles[0] || 'Huésped'}
                </span>
            </header>

            <div className="gh-profile-grid">
                <article className="gh-card">
                    <span className="gh-tag">Identidad</span>
                    <h2>{claims.name || account?.name || account?.username}</h2>
                    <div className="gh-detail-list">
                        <div className="gh-detail-row"><span>Correo</span><strong>{account?.username}</strong></div>
                        <div className="gh-detail-row"><span>Rol</span><strong>{roles.length > 0 ? roles.join(', ') : '(sin rol)'}</strong></div>
                        <div className="gh-detail-row"><span>Scopes</span><strong>{scopes.length > 0 ? scopes.join(', ') : '(sin scopes)'}</strong></div>
                    </div>
                </article>

                <article className="gh-card">
                    <span className="gh-tag">Resumen</span>
                    <h2>Su actividad</h2>
                    {errorDashboard && <p className="gh-error-text">{errorDashboard}</p>}
                    {!errorDashboard && !dashboard && <p>Cargando resumen...</p>}
                    {dashboard && (
                        <div className="gh-detail-list">
                            <div className="gh-detail-row"><span>Usuario</span><strong>{textoUsuario(dashboard.usuario)}</strong></div>
                            <div className="gh-detail-row"><span>Reservas</span><strong>{textoLista(dashboard.reservas, 'reservas')}</strong></div>
                            <div className="gh-detail-row"><span>Servicios en catálogo</span><strong>{textoLista(dashboard.servicios, 'servicios')}</strong></div>
                        </div>
                    )}
                </article>
            </div>
        </section>
    )
}

function textoUsuario(campo) {
    if (!campo) return '(sin datos)'
    if (campo.error) return `Error: ${campo.error}`
    const u = campo.datos || {}
    return `${u.nombre || '(sin nombre)'} (id ${u.id})`
}

function textoLista(campo, etiqueta) {
    if (!campo) return '(sin datos)'
    if (campo.error) return `Error: ${campo.error}`
    const lista = Array.isArray(campo.datos) ? campo.datos : []
    return `${lista.length} ${etiqueta}`
}

export default Mi_cuenta
