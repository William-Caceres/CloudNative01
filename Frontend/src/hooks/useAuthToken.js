import { useEffect, useState } from "react"
import { useMsal } from "@azure/msal-react"

function useAuthToken() {

    const { instance, accounts, inProgress } = useMsal()

    const [token, setToken] = useState(null)
    const [account, setAccount] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {

        const obtenerToken = async () => {

            if (inProgress !== "none") {return}
            if (accounts.length === 0) {
                setAccount(null)
                setToken(null)
                setLoading(false)
                return
            }

            try{
                const cuenta = accounts[0]
                setAccount(cuenta)
                const req = {
                    scopes: ["api://0dffbac7-2223-48cb-85c8-ba2376e4e633/api_access"],
                    account: cuenta
                }
                const respuesta = await instance.acquireTokenSilent(req)
                setToken(respuesta.accessToken)
                setError(null)
            }catch(error){
                console.error("ERROR al recuperar el TOKEN:", error)
                setToken(null)
                setError(error)
            }finally{
                setLoading(false)
            }
        }
        obtenerToken()
    }, [accounts, inProgress, instance])

    return {token,account,loading,error}
}
export default useAuthToken