import { useEffect, useState } from "react";

export function useDebounce<T>(value: T, delay = 300) {
  const [debounce, setDebounce] = useState(value);

  useEffect(() => {
    const t = setTimeout(() => setDebounce(value), delay);
    return () => clearTimeout(t);
  }, [value, delay]);

  return debounce;
}
