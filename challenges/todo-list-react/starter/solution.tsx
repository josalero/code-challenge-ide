import { useState } from "react";

export type Todo = { id: number; text: string; done: boolean };

export function TodoList() {
  const [todos, setTodos] = useState<Todo[]>([]);
  const [text, setText] = useState("");
  return (
    <div>
      <input aria-label="new todo" value={text} onChange={(e) => setText(e.target.value)} />
      <ul>{todos.map((t) => <li key={t.id}>{t.text}</li>)}</ul>
    </div>
  );
}
