import { Navigate } from "react-router-dom";
import { useIsAuthenticated } from "@azure/msal-react";

function UnProtectedRoute({children}){

    const isAuthenticated = useIsAuthenticated()

    if(isAuthenticated){
        return <Navigate to="/home" replace/>
    }
    return children
}

export default UnProtectedRoute