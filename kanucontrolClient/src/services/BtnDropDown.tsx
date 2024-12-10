import { Dropdown } from "primereact/dropdown";

interface MyDropdownChangeEvent {
	originalEvent?: React.SyntheticEvent<Element, Event>;
	value: any;
}

interface Item {
	name: string;
	code: string;
}

interface Props {
	label: string;
	labelPlaceholder: string;
	selectedItem: Item | null;
	items: Item[];
	setSelectedItem: (item: Item | null) => boolean;
}

export function BtnDropDown({
	label,
	labelPlaceholder,
	selectedItem,
	items,
	setSelectedItem,
}: Readonly<Props>) {
	const handleChange = (e: MyDropdownChangeEvent) => {
		setSelectedItem(e.value as Item | null);
	};

	return (
		<div className=" col-fixed">
			<div className="p-0">
				<span className="p-float-label">
					<Dropdown
						optionLabel="name"
						optionValue="code"
						value={selectedItem}
						options={items}
						onChange={handleChange}
						placeholder={labelPlaceholder}
					/>
					<label htmlFor="in">{label}</label>
				</span>
			</div>
		</div>
	);
}
