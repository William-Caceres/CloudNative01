import { Link, NavLink } from 'react-router-dom'
import { useIsAuthenticated, useMsal } from '@azure/msal-react'
import BrandMark from '../components/BrandMark'
import { getRoles } from '../auth/authUtils'

function navClass({ isActive }) {
  return isActive ? 'gh-nav-link active' : 'gh-nav-link'
}

function Navbar() {

    const isAuthenticated = useIsAuthenticated()
    const { instance } = useMsal()
    const account = instance.getAllAccounts()[0]
    const roles = getRoles(account)
    const isAdmin = roles.includes('Admin')

    const cerrarSesion = () => {
        instance.logoutRedirect()
    }

    return(
        <header className="gh-nav">
            <div className="gh-nav-inner">
                <Link className="gh-brand-link" to="/" aria-label="Grand Hotel Group, inicio">
                    <BrandMark />
                </Link>

                <nav className="gh-nav-links" aria-label="Navegación principal">
                    <NavLink to="/" end className={navClass}>Inicio</NavLink>
                    <NavLink to="/servicios" className={navClass}>Servicios</NavLink>
                    {isAuthenticated ? (
                        <>
                            <NavLink to="/mis_reservas" className={navClass}>Mis Reservas</NavLink>
                            <NavLink to="/mi_cuenta" className={navClass}>Mi cuenta</NavLink>
                            {isAdmin && <NavLink to="/PanelAdministradores" className={navClass}>Panel admin</NavLink>}
                            <button type="button" className="gh-nav-link" onClick={cerrarSesion}>Cerrar sesión</button>
                        </>
                    ) : (
                        <NavLink to="/login" className={navClass}>Iniciar sesión</NavLink>
                    )}
                </nav>

                {isAuthenticated && account && (
                    <span className="gh-user-pill" title={account.username}>
                        {roles[0] || 'Huésped'} · {account.name || account.username}
                    </span>
                )}
            </div>
        </header>
    )
}

export default Navbar
