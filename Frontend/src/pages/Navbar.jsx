import { useEffect, useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useIsAuthenticated, useMsal } from '@azure/msal-react'
import useAuthToken from '../hooks/useAuthToken'

function Navbar() {

    const isAuthenticated = useIsAuthenticated()
    const {instance} = useMsal()
    const {token, loading} = useAuthToken()
    
    const cerrarSesion = () => {
        instance.logoutRedirect()
    }

    let isAdmin = false

    if(!loading && token){
        const payload = JSON.parse(atob(token.split(".")[1]))
        if(payload.roles?.includes("admin")){
            isAdmin = true
        }
    }
    
    return(
        <>
        <nav className="navbar navbar-expand-lg bg-body-tertiary pad-0">
            <div className="container-fluid navbar_style">

                <Link className="navbar-brand force_c_white" to="/home">Grand Hotel</Link>
                <button className="navbar-toggler force_c_white force_border" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
                    <span className="force_c_white">""</span>
                </button>

                <div className="collapse navbar-collapse" id="navbarNavDropdown">
                    <ul className="navbar-nav">
                        {isAuthenticated ? (
                            <>
                            <li className="nav-item">
                                <Link className="nav-link active nav_link_style" aria-current="page" to="/home">Inicio</Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link active nav_link_style" aria-current="page" to="/servicios">Servicios disponibles</Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link active nav_link_style" aria-current="page" to="/mis_reservas">Mis Reservas</Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link active nav_link_style" aria-current="page" to="/mi_cuenta">Mi cuenta</Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link active nav_link_style" aria-current="page" to="#" onClick={cerrarSesion}>Cerrar sesion</Link>
                            </li>
                            {isAdmin &&
                                <li className="nav-item">
                                    <Link className="nav-link active nav_link_style" aria-current="page" to="/PanelAdministradores">Panel admin</Link>
                                </li>
                            }
                            </>
                        ) : (
                            <>
                            
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
/*
<li className="nav-item">
    <Link className="nav-link active nav_link_style" aria-current="page" to="/registro">Registrarme</Link>
</li>
<li className="nav-item">
    <Link className="nav-link active nav_link_style" aria-current="page" to="/login">Iniciar sesion</Link>
</li>
*/