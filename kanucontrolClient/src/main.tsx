import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import Root from "@/Root";
import { AppProvider } from "@/context/AppProvider";// 👈 NEU
import { LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import dayjs from "dayjs";
import "dayjs/locale/de";

dayjs.locale("de");

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <BrowserRouter>
      <LocalizationProvider dateAdapter={AdapterDayjs}>
        <AppProvider>
          {" "}
          {/* 👈 HIER */}
          <Root />
        </AppProvider>
      </LocalizationProvider>
    </BrowserRouter>
  </React.StrictMode>,
);
