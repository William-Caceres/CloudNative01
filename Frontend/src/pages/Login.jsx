import {useIsAuthenticated, useMsal} from "@azure/msal-react"

function Login() {

    const {instance} = useMsal()
    
    const isAuthenticated = useIsAuthenticated()

    const iniciarSesion = () => {
        instance.loginRedirect()
    }

    return(
        <>
            <div>
                <h2>Iniciar sesion</h2>
                <p>Inicia sesion con Microsoft</p>
                <button onClick={iniciarSesion}>Iniciar sesion</button>
            </div>
        </>
    )
}

export default Login