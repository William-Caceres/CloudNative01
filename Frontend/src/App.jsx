import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom'

import Navbar from './pages/Navbar'
import Inicio from './pages/Inicio'
import Registrarse from './pages/Registrarse'
import Login from './pages/Login'
import Mi_cuenta from './pages/Mi_cuenta'
import Servicios from './pages/Servicios'
import Servicios_detalle from './pages/Servicios_detalle'
import Reservas from './pages/Reservas'
import Reservas_detalle from './pages/Reservas_detalle'
import Panel_admin from './pages/Panel_admin'

import "./App.css"

function App(){
  
  return(
    <>
      <Navbar />
        <Routes>
          <Route path="/" element={<Inicio/>}/>
          <Route path="/registro" element={<Registrarse/>}/>
          <Route path="/login" element={<Login/>}/>
          <Route path="/mi_cuenta" element={<Mi_cuenta/>}/>
          <Route path="/servicios" element={<Servicios/>}/>
          <Route path="/servicios/detalle" element={<Servicios_detalle/>}/>
          <Route path="/mis_reservas" element={<Reservas/>}/>
          <Route path="/mis_reservas/detalle" element={<Reservas_detalle/>}/>
          <Route path="/PanelAdministradores" element={<Panel_admin/>}/>
        </Routes>
    </>
  )
}

export default App