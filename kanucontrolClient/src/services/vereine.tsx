import axios from "axios";
import { Verein } from "../components/interfaces/Verein";

const baseURL = "http://localhost:8080/verein";

const getCollectionURL = () => baseURL;

const getElementURL = (vereinId: number ) =>
  `${getCollectionURL()}/${encodeURIComponent(vereinId)}`;

export const getAllVereine = async () => {
  try {
    const response = await axios.get(getCollectionURL());
    const vereine = response.data;
    
    return vereine;
  } catch (error) {
    // log and rethrow 
    console.log(error);
    throw error;
  }
};

export const createVerein = async (verein: Verein) => {
  try {
    const response = await axios.post(getCollectionURL(), verein, {
      headers: { "Content-Type": "application/json" },
    });
    return response.data;
  } catch (error) {
    // log and rethrow 
    console.log(error);
    throw error;
  }
};

export const replaceVerein = async (verein: Verein) => {
  try {
    if (verein.id === undefined) {
      throw new Error("Verein ID is missing for replacement.");
    }
    
    const response = await axios.put(getElementURL(verein.id), verein, {
      headers: { "Content-Type": "application/json" },
    });
    return response.data;
  } catch (error) {
    // log and rethrow 
    console.log(error);
    throw error;
  }
};


export const deleteVerein = async (vereinId: number) => {
  try {
    await axios.delete(getElementURL(vereinId));
  } catch (error) {
    // log and rethrow 
    console.log(error);
    throw error;
  }
};
