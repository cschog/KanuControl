import { useContext, useEffect } from "react";
import { UNSAFE_NavigationContext } from "react-router-dom";

/** BrowserRouter unterstützt useBlocker nicht. Seine Navigation während einer Bearbeitung schützen. */
export function useVeranstaltungNavigationGuard(dirty: boolean) {
  const context = useContext(UNSAFE_NavigationContext);
  useEffect(() => {
    const unload = (event: BeforeUnloadEvent) => { if (dirty) { event.preventDefault(); event.returnValue = ""; } };
    window.addEventListener("beforeunload", unload);
    const navigator = context?.navigator;
    if (!dirty || !navigator) return () => window.removeEventListener("beforeunload", unload);
    const push = navigator.push.bind(navigator), replace = navigator.replace.bind(navigator), go = navigator.go.bind(navigator);
    const allow = () => window.confirm("Ungespeicherte Änderungen verwerfen?");
    navigator.push = (...args) => { if (allow()) push(...args); };
    navigator.replace = (...args) => { if (allow()) replace(...args); };
    navigator.go = (...args) => { if (allow()) go(...args); };
    return () => { navigator.push = push; navigator.replace = replace; navigator.go = go; window.removeEventListener("beforeunload", unload); };
  }, [dirty, context?.navigator]);
}
