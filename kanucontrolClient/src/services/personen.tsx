import apiClient from "./apiClient";
import { Person } from "../components/interfaces/Person";

const getCollectionURL = () => {
	// baseURL is defined in apiClient, so just return the root path for 'person' endpoint
	return "/person";
};

const getElementURL = (personId: number) =>
	`${getCollectionURL()}/${encodeURIComponent(personId)}`;

export const getAllPersonen = async () => {
	try {
		const response = await apiClient.get(getCollectionURL());
		console.log("getAllPersonen:", getCollectionURL());
		const personen = response.data;
		return personen;
	} catch (error) {
		throw error;
	}
};

export const createPerson = async (person: Person) => {
	try {
		const response = await apiClient.post(getCollectionURL(), person);
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

		const response = await apiClient.put(getElementURL(person.id), person);
		return response.data;
	} catch (error) {
		throw error;
	}
};

export const deletePerson = async (personId: number) => {
	try {
		await apiClient.delete(getElementURL(personId));
	} catch (error) {
		throw error;
	}
};
