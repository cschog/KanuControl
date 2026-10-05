import { useEffect, useState } from "react";

function ActiveSchema() {
  const [schema, setSchema] = useState("");

  useEffect(() => {
    fetch("/api/active-schema")
      .then((response) => response.text())
      .then((data) => setSchema(data))
      .catch((error) => console.error("Error fetching schema:", error));
  }, []);

  return <div>Active Schema: {schema}</div>;
}

export default ActiveSchema;