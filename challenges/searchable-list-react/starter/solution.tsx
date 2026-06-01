type Props = { items: string[] };

export function SearchableList({ items }: Props) {
  return (
    <div>
      <input aria-label="search" />
      <ul>{items.map((i) => <li key={i}>{i}</li>)}</ul>
    </div>
  );
}
