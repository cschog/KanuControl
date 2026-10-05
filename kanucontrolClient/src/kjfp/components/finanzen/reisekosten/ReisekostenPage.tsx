// src/components/finanzen/ReisekostenPage.tsx

import ReisekostenTable from "@/kjfp/components/finanzen/reisekosten/ReisekostenTable";

interface Props {
  veranstaltungId: number;
}

const ReisekostenPage = ({ veranstaltungId }: Props) => {
  return <ReisekostenTable veranstaltungId={veranstaltungId} />;
};

export default ReisekostenPage;
