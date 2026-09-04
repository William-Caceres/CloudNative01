import { Routes, Route, useLocation } from 'react-router-dom'

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
import ProtectedRoute from './components/ProtectedRoute'
import UnProtectedRoute from './components/UnProtectedRoute'

function App(){
  
  return(
    <>
      <Navbar />
        <Routes>
          <Route path="/" element={<Inicio/>}/>
          <Route path="/registro" element={<Registrarse/>}/>
          <Route path="/login" element={<UnProtectedRoute><Login/></UnProtectedRoute>}/>
          <Route path="/mi_cuenta" element={<ProtectedRoute><Mi_cuenta/></ProtectedRoute>}/>
          <Route path="/servicios" element={<Servicios/>}/>
          <Route path="/servicios/detalle" element={<Servicios_detalle/>}/>
          <Route path="/mis_reservas" element={<ProtectedRoute><Reservas/></ProtectedRoute>}/>
          <Route path="/mis_reservas/detalle" element={<ProtectedRoute><Reservas_detalle/></ProtectedRoute>}/>
          <Route path="/PanelAdministradores" element={<ProtectedRoute><Panel_admin/></ProtectedRoute>}/>
        </Routes>
    </>
  )
}

export default App