import apiClient from "./apiClient";
import { Verein } from "../components/interfaces/Verein";

// Fetch all Vereine
export const getAllVereine = async (): Promise<Verein[]> => {
  try {
    const response = await apiClient.get("/verein");
    return response.data;
  } catch (error) {
    console.error("Error in getAllVereine:", error);
    throw error;
  }
};

// Create a Verein
export const createVerein = async (verein: Verein): Promise<Verein> => {
  try {
    const response = await apiClient.post("/verein", verein);
    return response.data;
  } catch (error) {
    console.error("Error in createVerein:", error);
    throw error;
  }
};

// Update a Verein
export const replaceVerein = async (verein: Verein): Promise<Verein> => {
  try {
    if (verein.id === undefined) {
      throw new Error("Verein ID is missing for replacement.");
    }
    const response = await apiClient.put(
      `/verein/${encodeURIComponent(verein.id)}`,
      verein
    );
    return response.data;
  } catch (error) {
    console.error("Error in replaceVerein:", error);
    throw error;
  }
};

// Delete a Verein
export const deleteVerein = async (vereinId: number): Promise<void> => {
  try {
    await apiClient.delete(`/verein/${encodeURIComponent(vereinId)}`);
  } catch (error) {
    console.error("Error in deleteVerein:", error);
    throw error;
  }
};
