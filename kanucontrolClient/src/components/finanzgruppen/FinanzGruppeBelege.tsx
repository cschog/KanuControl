import { useEffect, useState } from "react";
import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import { AbrechnungBeleg } from "@/api/types/abrechnung";
import { getBelegeByFinanzGruppe } from "@/api/services/abrechnungApi";
import { belegColumns } from "@/components/finanzen/belegColumns"; 

interface Props {
  veranstaltungId: number;
  finanzGruppeId: number;
}

export default function FinanzGruppeBelege({ veranstaltungId, finanzGruppeId }: Props) {
  const [belege, setBelege] = useState<AbrechnungBeleg[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);

      try {
        const data = await getBelegeByFinanzGruppe(veranstaltungId, finanzGruppeId);

        if (!cancelled) {
          setBelege(data);
        }
      } catch (err) {
        console.error("Fehler beim Laden der Belege", err);

        if (!cancelled) {
          setBelege([]);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    load();

    return () => {
      cancelled = true;
    };
  }, [veranstaltungId, finanzGruppeId]);

  return (
    <GenericTableTanstack
      data={belege}
      columns={belegColumns}
      loading={loading}
      height={250}
      fixedColumnWidths={false}
    />
  );
}
