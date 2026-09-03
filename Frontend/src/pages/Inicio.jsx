import { useEffect, useState } from 'react'
import { useNavigate } from "react-router-dom"


function Inicio() {

    const navegar = useNavigate()

    return(
        <>
            <div className="p-4 div_style">
                <h1>Inicio</h1>
            </div>
        </>
    )
}

export default Inicio