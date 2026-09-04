import { Navigate } from "react-router-dom";
import { useIsAuthenticated } from "@azure/msal-react";

function ProtectedRoute({children}){

    const isAuthenticated = useIsAuthenticated()

    if(!isAuthenticated){
        return <Navigate to="/login" replace/>
    }
    return children
}

export default ProtectedRoute