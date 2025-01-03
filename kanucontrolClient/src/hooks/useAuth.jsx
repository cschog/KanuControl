import { useState, useEffect, useRef } from "react";
import keycloak from "../keycloak";

const useAuth = () => {
    const isRun = useRef(false);
    const [isLogin, setLogin] = useState(false);

    useEffect(() => {
        
        if(isRun.current) return

        isRun.current = true;

        keycloak.init({ onLoad: "login-required" })
            .then((authenticated) => {
                setLogin(authenticated);
            })
            .catch((error) => {
                console.error("Keycloak initialization failed", error);
            });
    }, []);

    return isLogin;
};

export default useAuth;