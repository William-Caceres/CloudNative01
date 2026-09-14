const API = "http://localhost:"

export const getSMT = async(puerto, api, ruta, token) => {
    try {
        const res = await fetch(`${API}${puerto}/${api}/${ruta}`,
        {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        })
        if(!res.ok) throw new Error("ERROR al ejecutar: ", ruta)
        const data =  await res.json()
        return data
    } catch(e) {
        console.error(e)
        return []
    }
}


export const getSMT_ID = async(puerto, api, ruta, id, token) => {
    try {
        const res = await fetch(`${API}${puerto}/${api}/${ruta}/${id}`,
        {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        })
        if(!res.ok) throw new Error("ERROR al ejecutar: ", ruta, ", ID: ", id)
        const data =  await res.json()
        return data
    } catch(e) {
        console.error(e)
        return null
    }
}

export const postSMT = async(puerto, api, ruta, cuerpo, token) => {
    try {
        console.log("ENVIANDO:", cuerpo)
        const res  = await fetch(`${API}${puerto}/${api}/${ruta}`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(cuerpo)
        })
        if(!res.ok) throw new Error("Error al ejecutar: ", ruta)
        const data = await res.json()
        return data
    } catch(e) {
        console.error("Couldnt execute "+`${API}${puerto}/${api}/${ruta}`)
        console.error(e)
        return e
    }
}

export const putSMT = async(puerto, api, ruta, cuerpo, token) => {
    try {
        console.log(cuerpo)
        const res  = await fetch(`${API}${puerto}/${api}/${ruta}`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(cuerpo)
        })
        if(!res.ok) throw new Error("Error al ejecutar: ", ruta)
        const data = await res.json()
        return data
    } catch(e) {
        console.error(e)
        return e
    }
}

export const deleteSMT = async(puerto, api, ruta, id, token) => {
    try {
        const res = await fetch(`${API}${puerto}/${api}/${ruta}/${id}`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        })
        if(!res.ok) throw new Error("Error al ejecutar: ", ruta)
        const data = await res.text()
        return data
    } catch(e) {
        console.error(e)
        return e
    }
}