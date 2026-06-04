import { Box } from "@mui/material";
import { DataGrid, GridColDef } from "@mui/x-data-grid";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { useAuditHistory } from "@/hooks/audit/useAuditHistory";

export default function AuditHistoryPage() {
  const navigate = useNavigate();


  const [paginationModel, setPaginationModel] = useState({
    page: 0,
    pageSize: 25,
  });

  const { data, isLoading } = useAuditHistory(paginationModel.page, paginationModel.pageSize);

  const columns: GridColDef[] = [
    {
      field: "username",
      headerName: "Benutzer",
      width: 150,
    },
    {
      field: "fullName",
      headerName: "Name",
      width: 220,
    },
    {
      field: "ipAddress",
      headerName: "IP",
      width: 150,
    },
    {
      field: "loginTime",
      headerName: "Login",
      width: 180,
      valueFormatter: (value) => (value ? new Date(value).toLocaleString("de-DE") : ""),
    },
    {
      field: "logoutTime",
      headerName: "Logout",
      width: 180,
      valueFormatter: (value) => (value ? new Date(value).toLocaleString("de-DE") : ""),
    },
  ];

  return (
    <Box>
      <DataGrid
        autoHeight
        loading={isLoading}
        rows={data?.content ?? []}
        columns={columns}
        rowCount={data?.totalElements ?? 0}
        paginationMode="server"
        paginationModel={paginationModel}
        onPaginationModelChange={setPaginationModel}
        pageSizeOptions={[10, 25, 50, 100]}
      />

      <BottomActionBar
        left={[
          {
            label: "Zurück",
            onClick: () => navigate("/admin"),
            variant: "outlined",
          },
        ]}
      />
    </Box>
  );
}
