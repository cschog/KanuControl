import App from "../App";          // The “protected” app
import Public from "../services/Public";
import useAuth from "../hooks/useAuth";

function Root() {
    const isLogin = useAuth();
  
    // Conditionally render either the full “App” or “Public”
    return isLogin ? <App /> : <Public />;
  }

  export default Root;