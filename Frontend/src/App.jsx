import Navbar from './pages/Navbar'
import RouterConfig from './routes/RouterConfig'

import "./App.css"

function App(){

  return(
    <div className="gh-app">
      <Navbar />
      <main className="gh-main">
        <RouterConfig />
      </main>
      <footer className="gh-footer">
        Grand Hotel Group · Hospitality, curated.
      </footer>
    </div>
  )
}

export default App
