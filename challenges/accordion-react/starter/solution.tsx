export type AccordionItem = { title: string; content: string };

type Props = { items: AccordionItem[] };

export function Accordion({ items }: Props) {
  return <div>{items.map((item) => <div key={item.title}><button type="button">{item.title}</button></div>)}</div>;
}
