import { Children, isValidElement, useEffect, useId, useRef, useState, type ReactNode } from "react";
import { Alert, Box, Button, CircularProgress, Dialog, DialogTitle, DialogContent, IconButton } from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import ReactMarkdown from "react-markdown";
import type { InfoPage } from "@/kjfp/api/enums/InfoPage";

export const PAGE_CONFIG = {
  help: { title: "Hilfeübersicht", file: "/info/bedienungsanleitung.md" },
  "help-kjfp": { title: "Bedienungsanleitung KJFP", file: "/info/bedienungsanleitung-kjfp.md" },
  "help-vermietung": { title: "Bedienungsanleitung Vermietung", file: "/info/bedienungsanleitung-vermietung.md" },
  homepage: { title: "Homepage-Anbindung", file: "/info/homepage-anbindung.md" },
  news: { title: "Was ist neu?", file: "/info/releases/releases.md" },
  privacy: { title: "Datenschutz", file: "/info/datenschutz.md" },
  imprint: { title: "Impressum", file: "/info/impressum.md" },
  about: { title: "Über KanuControl", file: "/info/about.md" },
} as const satisfies Record<InfoPage, { title: string; file: string }>;

type Props = { open: boolean; onClose: () => void; page: InfoPage };
type DocumentState = { file: string; markdown?: string; error?: string };

function textOf(node: ReactNode): string {
  return Children.toArray(node).map(child => isValidElement<{ children?: ReactNode }>(child)
    ? textOf(child.props.children) : typeof child === "string" || typeof child === "number" ? String(child) : "").join("");
}
function headingId(children: ReactNode): string {
  return textOf(children).replace(/ß/g, "ss").normalize("NFKD").replace(/\p{M}/gu, "")
    .toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");
}

export const InfoDialog = ({ open, onClose, page }: Props) => {
  const [activePage, setActivePage] = useState<InfoPage>(page);
  const [document, setDocument] = useState<DocumentState>({ file: "" });
  const [retry, setRetry] = useState(0);
  const [anchor, setAnchor] = useState<string | null>(null);
  const contentRef = useRef<HTMLDivElement>(null);
  const titleId = useId();
  const config = PAGE_CONFIG[activePage];
  const loading = document.file !== config.file || (!document.markdown && !document.error);

  useEffect(() => { if (open) { setActivePage(page); setAnchor(null); } }, [open, page]);

  useEffect(() => {
    if (!open) return;
    const controller = new AbortController();
    setDocument({ file: config.file });
    contentRef.current?.scrollTo({ top: 0 });
    const load = async () => {
      try {
        const response = await fetch(config.file, { signal: controller.signal, cache: "no-cache" });
        if (!response.ok || response.headers.get("content-type")?.includes("text/html")) throw new Error("Hilfe nicht verfügbar");
        const markdown = await response.text();
        // Bei einem SPA-Fallback darf die HTML-Startseite nicht als Anleitung erscheinen.
        if (!markdown.trim() || /^\s*<(?:!doctype|html)\b/i.test(markdown)) throw new Error("Ungültige Hilfedatei");
        if (!controller.signal.aborted) setDocument({ file: config.file, markdown });
      } catch {
        if (!controller.signal.aborted) setDocument({ file: config.file, error: "Die Hilfe konnte nicht geladen werden. Bitte erneut versuchen." });
      }
    };
    void load();
    return () => controller.abort();
  }, [open, config.file, retry]);

  useEffect(() => {
    if (loading || !anchor || !document.markdown) return;
    globalThis.document.getElementById(anchor)?.scrollIntoView({ block: "start" });
    setAnchor(null);
  }, [loading, anchor, document.markdown]);

  function navigate(target: InfoPage, fragment?: string) {
    setAnchor(fragment ? decodeURIComponent(fragment) : null);
    setActivePage(target);
    if (!fragment) contentRef.current?.scrollTo({ top: 0 });
  }

  return <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth aria-labelledby={titleId}>
    <DialogTitle id={titleId} sx={{ pr: 7 }}>
      {config.title}
    </DialogTitle>
    <IconButton aria-label="Hilfe schließen" onClick={onClose} sx={{ position: "absolute", right: 8, top: 8 }}>
      <CloseIcon />
    </IconButton>
    <DialogContent ref={contentRef} dividers sx={{ overflowWrap: "anywhere",
      "& h1": { fontSize: "1.65rem" }, "& h2": { fontSize: "1.3rem", mt: 3, scrollMarginTop: 12 },
      "& h3": { fontSize: "1.1rem", mt: 2, scrollMarginTop: 12 }, "& h4": { fontSize: "1rem" },
      "& pre": { p: 1.5, bgcolor: "action.hover", overflowX: "auto", borderRadius: 1 },
      "& pre code": { whiteSpace: "pre", overflowWrap: "normal" }, "& li": { mb: 0.5 }, "& a": { color: "primary.main" },
    }}>
      {activePage !== "help" && <Button size="small" onClick={() => navigate("help")}>Hilfeübersicht</Button>}
      {loading ? <Box sx={{ py: 4, textAlign: "center" }}><CircularProgress aria-label="Hilfe wird geladen" /></Box>
        : document.error ? <Alert severity="error" action={<Button color="inherit" onClick={() => setRetry(n => n + 1)}>Erneut laden</Button>}>{document.error}</Alert>
        : <ReactMarkdown components={{
          h1: ({ children }) => <h1 id={headingId(children)}>{children}</h1>,
          h2: ({ children }) => <h2 id={headingId(children)}>{children}</h2>,
          h3: ({ children }) => <h3 id={headingId(children)}>{children}</h3>,
          h4: ({ children }) => <h4 id={headingId(children)}>{children}</h4>,
          a: ({ href, children }) => {
            const [file, fragment] = (href ?? "").split("#");
            const target = (Object.keys(PAGE_CONFIG) as InfoPage[]).find(key => PAGE_CONFIG[key].file === file);
            if (target || href?.startsWith("#")) return <a href={href} onClick={event => {
              event.preventDefault(); navigate(target ?? activePage, fragment);
            }}>{children}</a>;
            return <a href={href} target="_blank" rel="noopener noreferrer">{children}</a>;
          },
        }}>{document.markdown}</ReactMarkdown>}
    </DialogContent>
  </Dialog>;
};
