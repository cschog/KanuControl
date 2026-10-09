import type { RouteObject } from "react-router-dom";

import AdminPage from "@/core/components/admin/AdminPage";
import PostalCodeAdminPage from "@/core/components/admin/PostalCodeAdminPage";
import FoerdersatzAdminPage from "@/core/components/admin/foerdersatz/FoerdersatzAdminPage";
import KikZuschlagAdminPage from "@/core/components/admin/kik/KikZuschlagAdminPage";
import ReisekostenKonfigurationPage from "@/core/components/admin/reisekosten/ReisekostenKonfigurationPage";
import ActiveSessionsPage from "@/core/components/admin/audit/ActiveSessionsPage";
import AuditHistoryPage from "@/core/components/admin/audit/AuditHistoryPage";
import AuditPage from "@/core/components/admin/audit/AuditPage";

const AdminRoutes: RouteObject[] = [
  {
    path: "/admin",
    element: <AdminPage />,
  },
  {
    path: "/admin/postal-codes",
    element: <PostalCodeAdminPage />,
  },
  {
    path: "/admin/foerdersaetze",
    element: <FoerdersatzAdminPage />,
  },
  {
    path: "/admin/kik-zuschlaege",
    element: <KikZuschlagAdminPage />,
  },
  {
    path: "/admin/reisekosten",
    element: <ReisekostenKonfigurationPage />,
  },
  {
    path: "/admin/audit/active-sessions",
    element: <ActiveSessionsPage />,
  },
  {
    path: "/admin/audit/history",
    element: <AuditHistoryPage />,
  },
  {
    path: "/admin/audit",
    element: <AuditPage />,
  },
];

export default AdminRoutes;
