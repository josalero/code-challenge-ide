import { useState } from "react";

type CounterProps = {
  initial?: number;
};

export function Counter({ initial = 0 }: CounterProps) {
  const [count] = useState(initial);
  return <button type="button">{count}</button>;
}
