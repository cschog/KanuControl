export function mapRoleFromBackend(role: string | null): "L" | "M" | null {
  if (role === "L") return "L";
  if (role === "M") return "M";
  return null;
}

export function mapRoleToBackend(role: "L" | "M" | null): string | null {
  if (role === "L") return "L";
  if (role === "M") return "M";
  return null;
}
