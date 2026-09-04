import {useMsal} from "@azure/msal-react"

function Mi_cuenta() {

    const {instance} = useMsal()

    const cerrarSesion = () => {
        instance.logoutRedirect()
    }

    return(
        <>
            <div>
                <h2>Mi cuenta</h2>
                <p>Nombre</p>
                <p>Correo</p>
                <p>Contraseña</p>
                <button onClick={cerrarSesion}>Cerrar sesion</button>
            </div>
        </>
    )
}

export default Mi_cuenta