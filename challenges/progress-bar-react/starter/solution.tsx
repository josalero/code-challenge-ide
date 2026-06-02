type Props = { percent: number };

export function ProgressBar({ percent }: Props) {
  return (
    <div role="progressbar" aria-valuenow={percent} aria-valuemin={0} aria-valuemax={100}>
      <div className="fill" />
    </div>
  );
}
