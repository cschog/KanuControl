import axios from "axios";
import { Person } from "../components/interfaces/Person";

const baseURL = "http://localhost:8080/person"; 

const getCollectionURL = () => baseURL;

const getElementURL = (personId: number) =>
  `${getCollectionURL()}/${encodeURIComponent(personId)}`;

export const getAllPersonen = async () => {
  try {
    const response = await axios.get(getCollectionURL());
    console.log("getAllPersonen:", getCollectionURL())
    const personen = response.data;
    return personen;
  } catch (error) {
    throw error;
  }
};

export const createPerson = async (person: Person) => {
  try {
    const response = await axios.post(getCollectionURL(), person, {
      headers: { "Content-Type": "application/json" },
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const replacePerson = async (person: Person) => {
  try {
    if (person.id === undefined) {
      throw new Error("Person ID is missing for replacement.");
    }

    const response = await axios.put(getElementURL(person.id), person, {
      headers: { "Content-Type": "application/json" },
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const deletePerson = async (personId: number) => {
  try {
    await axios.delete(getElementURL(personId));
  } catch (error) {
    throw error;
  }
};
