// UnterkunftsartAutocomplete.tsx

import { RefAutocomplete } from "@/components/common/RefAutocomplete";

import { useUnterkunftsarten } from "@/hooks/useUnterkunftsarten";

import { UnterkunftsartRef } from "@/api/types/unterkunft/UnterkunftsartRef";

interface Props {

    value?: UnterkunftsartRef | null;

    onChange: (v: UnterkunftsartRef | null) => void;

    disabled?: boolean;
}

export function UnterkunftsartAutocomplete(props: Props) {

    const {
        options,
        loading,
    } = useUnterkunftsarten();

    return (
        <RefAutocomplete
            {...props}
            options={options}
            loading={loading}
            label="Unterkunftsart"
        />
    );
}