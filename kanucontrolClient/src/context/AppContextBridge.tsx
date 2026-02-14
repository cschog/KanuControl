import { useAppContext } from "./AppContext";

export function useReloadAppContext() {
  const { reload } = useAppContext();
  return reload;
}
