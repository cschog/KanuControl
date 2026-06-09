import apiClient from "@/api/client/apiClient";

export async function getOnlineUsers(): Promise<string[]> {
  const response = await apiClient.get<string[]>("/session/online-users");

  return response.data;
}
