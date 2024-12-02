import axios from "axios";
import  VereinDTO from "../components/interfaces/VereinDTO";

const baseURL = "http://localhost:8080/verein";

const getCollectionURL = () => baseURL;
const getElementURL = (vereinId: number) => `${getCollectionURL()}/${encodeURIComponent(vereinId)}`;

export const getAllVereine = async (): Promise<VereinDTO[]> => {
  try {
    const response = await axios.get<VereinDTO[]>(getCollectionURL());
    return response.data; // DTO format expected
  } catch (error) {
    console.error("Error fetching Vereine:", error);
    throw error;
  }
};

export const createVerein = async (verein: VereinDTO): Promise<VereinDTO> => {
  try {
    const response = await axios.post<VereinDTO>(getCollectionURL(), verein, {
      headers: { "Content-Type": "application/json" },
    });
    return response.data;
  } catch (error) {
    console.error("Error creating Verein:", error);
    throw error;
  }
};

export const replaceVerein = async (verein: VereinDTO): Promise<VereinDTO> => {
  if (!verein.id) throw new Error("Verein ID is required for replacement.");
  try {
    const response = await axios.put<VereinDTO>(getElementURL(verein.id), verein, {
      headers: { "Content-Type": "application/json" },
    });
    return response.data;
  } catch (error) {
    console.error("Error updating Verein:", error);
    throw error;
  }
};

export const deleteVerein = async (vereinId: number): Promise<void> => {
  try {
    await axios.delete(getElementURL(vereinId));
  } catch (error) {
    console.error("Error deleting Verein:", error);
    throw error;
  }
};