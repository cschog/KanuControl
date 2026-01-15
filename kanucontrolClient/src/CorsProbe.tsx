import { useEffect } from "react";

export default function CorsProbe() {
  useEffect(() => {
    fetch("http://localhost:8090/api/test-cors")
      .then(res => res.text())
      .then(data => console.log("CORS RESULT:", data))
      .catch(err => console.error("CORS ERROR", err));
  }, []);

  return <div>CORS Probe – siehe Konsole</div>;
}