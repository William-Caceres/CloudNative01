import {PublicClientApplication, IPublicClientApplication, LogLevel} from "@azure/msal-browser";
import {environment} from "./enviroment";

export const msalInstance = new PublicClientApplication ({

    auth: {
        clientId: environment.azure.clientId,
        authority: environment.azure.authority,
        redirectUri: environment.azure.redirectUri
    },
    cache: {
        cacheLocation: "localStorage"
    }
});