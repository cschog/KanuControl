import React from "react";

interface LoadingOrErrorProps {
  loading: boolean;
  error: string | null;
}
// loadingOrErrorUtils.js
export const renderLoadingOrError = ({
  loading,
  error,
}: LoadingOrErrorProps) => {
  if (loading) {
    return <div>Loading...</div>;
  }
  if (error) {
    return <div>Error: {error}</div>;
  }
  return null;
};
