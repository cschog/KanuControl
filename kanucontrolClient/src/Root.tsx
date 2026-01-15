import { useAuth } from "@/auth/useAuth";
import TenantProbe from "@/debug/TenantProbe";
import Public from "@/pages/Public";

function Root() {
  const { initialized, authenticated } = useAuth();

  if (!initialized) {
    return <div>Lade Auth …</div>;
  }

  return authenticated ? <TenantProbe /> : <Public />;
}

export default Root;