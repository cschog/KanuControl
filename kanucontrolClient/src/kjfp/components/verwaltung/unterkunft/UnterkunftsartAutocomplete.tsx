// UnterkunftsartAutocomplete.tsx

import { RefAutocomplete } from "@/core/components/common/RefAutocomplete";

import { useUnterkunftsarten } from "@/kjfp/hooks/useUnterkunftsarten";

import { UnterkunftsartRef } from "@/kjfp/types/unterkunft/UnterkunftsartRef";

interface Props {
  value?: UnterkunftsartRef | null;

  onChange: (v: UnterkunftsartRef | null) => void;

  disabled?: boolean;
}

export function UnterkunftsartAutocomplete(props: Props) {
  const { options, loading } = useUnterkunftsarten();

  return <RefAutocomplete {...props} options={options} loading={loading} label="Unterkunftsart" />;
}
