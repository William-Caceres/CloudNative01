export const environment = {

    production: false,

    azure: {
        clientId: "46cda96c-1ab7-42af-a838-5ad46de31226",
        tenantId: "e34a6311-e69d-4a97-bedf-2ad7cc5455a0",
        authority: "https://login.microsoftonline.com/e34a6311-e69d-4a97-bedf-2ad7cc5455a0",
        redirectUri: window.location.origin
    },

    apiBaseUrl: import.meta.env.VITE_API_BASE_URL || "https://z8bmmvfdwh.execute-api.us-east-1.amazonaws.com"
}
