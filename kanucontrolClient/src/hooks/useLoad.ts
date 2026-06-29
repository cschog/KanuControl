import { useCallback, useEffect, useState } from "react";

/* =========================================================
   OPTIONS
   ========================================================= */

interface UseLoadOptions<T> {
  initialData?: T;
  autoLoad?: boolean;
}

/* =========================================================
   OVERLOADS
   ========================================================= */

export function useLoad<T>(
  loader: () => Promise<T>,
  options: {
    initialData: T;
    autoLoad?: boolean;
  },
): {
  data: T;
  loading: boolean;
  error: unknown;
  reload: () => Promise<T>;
  setData: React.Dispatch<React.SetStateAction<T>>;
};

export function useLoad<T>(
  loader: () => Promise<T>,
  options?: {
    autoLoad?: boolean;
  },
): {
  data: T | undefined;
  loading: boolean;
  error: unknown;
  reload: () => Promise<T>;
  setData: React.Dispatch<React.SetStateAction<T | undefined>>;
};

/* =========================================================
   IMPLEMENTATION
   ========================================================= */

export function useLoad<T>(
  loader: () => Promise<T>,
  {
    initialData,
    autoLoad = true,
  }: UseLoadOptions<T> = {},
) {
  const [data, setData] = useState<T | undefined>(initialData);

  const [loading, setLoading] = useState(false);

  const [error, setError] = useState<unknown>(null);

  const reload = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const result = await loader();

      setData(result);

      return result;
    } catch (err) {
      setError(err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [loader]);

  useEffect(() => {
    if (autoLoad) {
      void reload();
    }
  }, [autoLoad, reload]);

  return {
    data,
    loading,
    error,
    reload,
    setData,
  };
}