import { Button } from "primereact/button";

interface BtnStoreCancelProps {
	createUpdate: () => boolean;
	onAbbruch: () => void;
}

export function BtnStoreCancel({
	createUpdate,
	onAbbruch,
  }: Readonly<BtnStoreCancelProps>) {
	const handleSave = () => {
	  const success = createUpdate();
	  if (!success) {
		console.error("Failed to save changes");
	  }
	};
  
	return (
	  <>
		<Button
		  label="Speichern"
		  className="p-button-outlined p-button-success m-2"
		  onClick={handleSave}
		/>
		<Button
		  label="Abbruch"
		  className="p-button-outlined m-2 ml-6"
		  onClick={onAbbruch}
		/>
	  </>
	);
  }
