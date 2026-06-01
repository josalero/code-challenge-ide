type Props = { max?: number; value?: number; onChange?: (value: number) => void };

export function StarRating({ max = 5, value = 0 }: Props) {
  return (
    <div role="group" aria-label="rating">
      {Array.from({ length: max }, (_, i) => (
        <button key={i} type="button" aria-label={`star ${i + 1}`}>☆</button>
      ))}
    </div>
  );
}
