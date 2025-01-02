import Protected from "./components/Protected";
import Public from "./components/Public";
import useAuth from "./hooks/UseAuth";

function App() {

  console.log(import.meta.env.VITE_KEYCLOAK_URL);
console.log(import.meta.env.VITE_KEYCLOAK_REALM);
console.log(import.meta.env.VITE_KEYCLOAK_CLIENT);
 
  const isLogin = useAuth();
  return isLogin ? <Protected/> : <Public/>;
}

export default App;
