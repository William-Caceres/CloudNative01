import { useEffect, useState } from 'react'
import { useNavigate } from "react-router-dom"
import Formulario_servicios from '../components/Formulario_servicios'


function Inicio() {

    const navegar = useNavigate()

    return(
        <>
            <div className="p-4 div_style">
                <h2>Bienvenido a Purple Hotel!</h2>
                <p>Consulta nuestros distintos servicios aqui:</p>
                <button onClick={()=>navegar("servicios")}>Servicios</button>
                <p>Consulta tus reservas aqui:</p>
                <button onClick={()=>navegar("mis_reservas")}>Mis reservas</button>
            </div>
            
        </>
    )
}

export default Inicio