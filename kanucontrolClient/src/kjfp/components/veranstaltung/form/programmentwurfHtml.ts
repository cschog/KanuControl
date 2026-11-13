// Nie fremdes HTML direkt in die Oberfläche einsetzen.
export function cleanProgramHtml(value: string | null | undefined): string {
  const source = new DOMParser().parseFromString(value ?? "", "text/html");
  source.querySelectorAll("script,style,iframe,object,embed,svg,math,template").forEach(n => n.remove());
  const target = document.createElement("div");
  const copy = (node: Node, into: HTMLElement, inList = false) => {
    if (node.nodeType === Node.TEXT_NODE) { into.append(document.createTextNode(node.textContent ?? "")); return; }
    if (!(node instanceof Element)) return;
    let tag = node.tagName.toLowerCase();
    if (tag === "b") tag = "strong";
    if (tag === "i") tag = "em";
    if (["div", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote", "pre", "tr", "td", "th"].includes(tag)) tag = "p";
    if (inList && (tag === "ul" || tag === "ol")) {
      node.childNodes.forEach(child => copy(child, into, true)); return;
    }
    if (inList && tag === "li") tag = "p";
    if (["p", "br", "strong", "em", "ul", "ol", "li"].includes(tag)) {
      const el = document.createElement(tag); into.append(el);
      node.childNodes.forEach(child => copy(child, el, inList || tag === "li"));
    } else node.childNodes.forEach(child => copy(child, into, inList));
  };
  source.body.childNodes.forEach(node => copy(node, target));
  return new DOMParser().parseFromString(target.innerHTML, "text/html").body.innerHTML;
}

export function programText(html: string | null | undefined): string {
  const doc = new DOMParser().parseFromString(cleanProgramHtml(html), "text/html");
  let result = "";
  const read = (n: Node) => {
    if (n.nodeType === Node.TEXT_NODE) result += n.textContent?.replace(/\u00a0/g, " ") ?? "";
    else if (n instanceof Element) {
      if (n.tagName === "BR") result += "\n";
      else { n.childNodes.forEach(read); if (["P", "LI"].includes(n.tagName)) result += "\n"; }
    }
  };
  doc.body.childNodes.forEach(read);
  return result.endsWith("\n") ? result.slice(0, -1) : result;
}
export const hasProgramText = (html: string | null | undefined) => /[^\s\u200b-\u200f\u202a-\u202e\u2060-\u206f\ufeff]/u.test(programText(html));
export function programErrors(title: string | null | undefined = "", html: string | null | undefined = ""): string[] {
  title = title ?? "";
  html = html ?? "";
  const errors: string[] = [];
  if (Array.from(title).length > 200 || /[\r\n]/.test(title)) errors.push("Die Überschrift darf höchstens 200 Zeichen enthalten und muss einzeilig sein.");
  if (Array.from(programText(html)).length > 50000) errors.push("Der Programmtext darf höchstens 50.000 sichtbare Zeichen enthalten.");
  if (new TextEncoder().encode(html).length > 500000) errors.push("Der Programmentwurf ist technisch zu groß (maximal 500.000 UTF-8-Bytes).");
  return errors;
}
