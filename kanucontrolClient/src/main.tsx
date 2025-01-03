// main.tsx (or main.jsx)
import React from "react";
import ReactDOM from "react-dom/client";
import Root from "./components/Root";
import "./index.css";  // ensure your global CSS is still here


// Then mount Root instead of App
ReactDOM.createRoot(document.getElementById("root")!).render(
  <Root />
);