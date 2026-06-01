type Props = { color: string };

export function ColorBox({ color }: Props) {
  return <div role="img" aria-label={color} />;
}
