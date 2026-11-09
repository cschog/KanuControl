import { lazy } from "react";
import type { RouteObject } from "react-router-dom";

const AdminPage = lazy(() => import("@/core/components/admin/AdminPage"));
const PostalCodeAdminPage = lazy(() => import("@/core/components/admin/PostalCodeAdminPage"));
const FoerdersatzAdminPage = lazy(() => import("@/core/components/admin/foerdersatz/FoerdersatzAdminPage"));
const KikZuschlagAdminPage = lazy(() => import("@/core/components/admin/kik/KikZuschlagAdminPage"));
const ReisekostenKonfigurationPage = lazy(() => import("@/core/components/admin/reisekosten/ReisekostenKonfigurationPage"));
const ActiveSessionsPage = lazy(() => import("@/core/components/admin/audit/ActiveSessionsPage"));
const AuditHistoryPage = lazy(() => import("@/core/components/admin/audit/AuditHistoryPage"));
const AuditPage = lazy(() => import("@/core/components/admin/audit/AuditPage"));

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
