import { useEffect, useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useIsAuthenticated, useMsal } from '@azure/msal-react'

function Navbar() {

    const isAuthenticated = useIsAuthenticated()

    const {instance} = useMsal()
    
    const cerrarSesion = () => {
        instance.logoutRedirect()
    }
    
    return(
        <>
        <nav className="navbar navbar-expand-lg bg-body-tertiary pad-0">
            <div className="container-fluid navbar_style">

                <Link className="navbar-brand force_c_white" to="/">Grand Hotel</Link>
                <button className="navbar-toggler force_c_white force_border" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
                    <span className="force_c_white">""</span>
                </button>

                <div className="collapse navbar-collapse" id="navbarNavDropdown">
                    <ul className="navbar-nav">
                        <li className="nav-item">
                            <Link className="nav-link active nav_link_style" aria-current="page" to="/">Inicio</Link>
                        </li>
                        <li className="nav-item">
                            <Link className="nav-link active nav_link_style" aria-current="page" to="/servicios">Servicios disponibles</Link>
                        </li>
                        {isAuthenticated ? (
                            <>
                            <li className="nav-item">
                                <Link className="nav-link active nav_link_style" aria-current="page" to="/reservas">Mis Reservas</Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link active nav_link_style" aria-current="page" to="/mi_cuenta">Mi cuenta</Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link active nav_link_style" aria-current="page" to="#" onClick={cerrarSesion}>Cerrar sesion</Link>
                            </li>
                            </>
                        ) : (
                            <>
                            <li className="nav-item">
                                <Link className="nav-link active nav_link_style" aria-current="page" to="/registro">Registrarme</Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link active nav_link_style" aria-current="page" to="/login">Iniciar sesion</Link>
                            </li>
                            </>
                        )}
                        
                    </ul>
                </div>

            </div>
        </nav>
        </>
    )
}

export default Navbar