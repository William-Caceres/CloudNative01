import { useEffect, useState } from 'react'
import { useMsal, useIsAuthenticated, useAccount } from '@azure/msal-react'
import BrandMark from '../components/BrandMark'
import { loginRequest } from '../auth/authConfig'
import { getRoles, getAccessTokenScopes } from '../auth/authUtils'

function Login() {
    const { instance } = useMsal()
    const isAuthenticated = useIsAuthenticated()
    const accounts = instance.getAllAccounts()
    const account = useAccount(accounts.length > 0 ? accounts[0] : null)
    const [scopes, setScopes] = useState([])

    const roles = getRoles(account)

    useEffect(() => {
        if (!isAuthenticated) return
        getAccessTokenScopes(instance).then(setScopes).catch(() => setScopes([]))
    }, [isAuthenticated, instance])

    const handleLogin = () => {
        instance.loginRedirect(loginRequest)
    }

    const handleLogout = () => {
        instance.logoutRedirect()
    }

    return (
        <section className="gh-auth">
            <div className="gh-auth-panel">
                <BrandMark />
                <div>
                    <p className="gh-eyebrow">Acceso privado</p>
                    <h1>Una experiencia pensada hasta el último detalle.</h1>
                    <p>Gestiona tus reservas, servicios y preferencias desde un solo lugar.</p>
                </div>
            </div>

            <div className="gh-auth-card">
                {!isAuthenticated ? (
                    <>
                        <p className="gh-eyebrow">Grand Hotel Group</p>
                        <h2>Iniciar sesión</h2>
                        <p>Accede con tu cuenta de Microsoft Entra ID.</p>
                        <button className="gh-btn gh-btn-primary" onClick={handleLogin}>
                            Iniciar sesión
                        </button>
                    </>
                ) : (
                    <>
                        <p className="gh-eyebrow">Sesión verificada</p>
                        <h2>Sesión activa</h2>
                        <p>Usuario: {account?.username}</p>
                        <p>Roles: {roles.length > 0 ? roles.join(', ') : '(sin roles)'}</p>
                        <p>Scopes: {scopes.length > 0 ? scopes.join(', ') : '(sin scopes)'}</p>
                        <button className="gh-btn gh-btn-secondary" onClick={handleLogout}>
                            Cerrar sesión
                        </button>
                    </>
                )}
            </div>
        </section>
    )
}

export default Login
