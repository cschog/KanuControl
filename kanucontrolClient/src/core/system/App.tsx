import React from "react";
import { useRoutes } from "react-router-dom";

import ModuleStart from "@/core/components/module/ModuleStart";
import AppLayout from "@/core/components/layout/AppLayout";
import StartMenue from "@/kjfp/components/startmenu/StartMenu";

import KjfpRoutes from "@/core/system/routes/KjfpRoutes";
import VermietungRoutes from "@/core/system/routes/VermietungRoutes";
import AdminRoutes from "@/core/system/routes/AdminRoutes";

const App: React.FC = () => {
  const routes = useRoutes([
    {
      element: <AppLayout />,
      children: [
        {
          path: "/",
          element: <ModuleStart />,
        },
        {
          path: "/startmenue",
          element: <StartMenue />,
        },

        ...KjfpRoutes,
        ...VermietungRoutes,
        ...AdminRoutes,
      ],
    },
  ]);

  return <div className="App">{routes}</div>;
};

export default App;
