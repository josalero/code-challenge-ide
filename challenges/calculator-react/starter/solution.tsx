import { useState } from "react";

export function Calculator() {
  const [a, setA] = useState(0);
  const [b, setB] = useState(0);
  return (
    <div>
      <input aria-label="a" type="number" value={a} onChange={() => {}} />
      <input aria-label="b" type="number" value={b} onChange={() => {}} />
      <span role="status">0</span>
    </div>
  );
}
