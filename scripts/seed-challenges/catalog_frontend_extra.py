"""Extra React / Vue / Angular challenges — app-ideas, vuejs/examples, angular-basics, RealWorld-style."""

FRONTEND_EXTRA_CHALLENGES = [
    # --- React (florinpop17/app-ideas, greatfrontend, bulletproof-react patterns) ---
    {
        "slug": "todo-list-react",
        "title": "Todo List (React)",
        "difficulty": "medium",
        "description": "Implement `TodoList` — text input adds todos; clicking a todo toggles `done` class on its `<li>`.",
        "language": "react",
        "runtime": "19",
        "starter": """import { useState } from "react";

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
""",
        "public_tests": [
            (
                "todo.test",
                """import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { TodoList } from "../solution";

describe("TodoList", () => {
  it("adds a todo on Enter", () => {
    render(<TodoList />);
    const input = screen.getByLabelText("new todo");
    fireEvent.change(input, { target: { value: "Buy milk" } });
    fireEvent.keyDown(input, { key: "Enter" });
    expect(screen.getByText("Buy milk")).toBeInTheDocument();
  });

  it("toggles done class", () => {
    render(<TodoList />);
    const input = screen.getByLabelText("new todo");
    fireEvent.change(input, { target: { value: "Task" } });
    fireEvent.keyDown(input, { key: "Enter" });
    fireEvent.click(screen.getByText("Task"));
    expect(screen.getByText("Task").closest("li")).toHaveClass("done");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "todo_hidden.test",
                """import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { TodoList } from "../solution";

describe("TodoList hidden", () => {
  it("adds two todos", () => {
    render(<TodoList />);
    const input = screen.getByLabelText("new todo");
    fireEvent.change(input, { target: { value: "A" } });
    fireEvent.keyDown(input, { key: "Enter" });
    fireEvent.change(input, { target: { value: "B" } });
    fireEvent.keyDown(input, { key: "Enter" });
    expect(screen.getByText("A")).toBeInTheDocument();
    expect(screen.getByText("B")).toBeInTheDocument();
  });
});
""",
            )
        ],
    },
    {
        "slug": "star-rating-react",
        "title": "Star Rating (React)",
        "difficulty": "easy",
        "description": "Implement `StarRating` with props `max` (default 5) and `value`; clicking star `n` sets value to `n`.",
        "language": "react",
        "runtime": "19",
        "starter": """type Props = { max?: number; value?: number; onChange?: (value: number) => void };

export function StarRating({ max = 5, value = 0 }: Props) {
  return (
    <div role="group" aria-label="rating">
      {Array.from({ length: max }, (_, i) => (
        <button key={i} type="button" aria-label={`star ${i + 1}`}>☆</button>
      ))}
    </div>
  );
}
""",
        "public_tests": [
            (
                "stars.test",
                """import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { StarRating } from "../solution";

describe("StarRating", () => {
  it("selects third star", () => {
    render(<StarRating />);
    fireEvent.click(screen.getByLabelText("star 3"));
    expect(screen.getByLabelText("star 3")).toHaveAttribute("aria-pressed", "true");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "stars_hidden.test",
                """import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { StarRating } from "../solution";

describe("StarRating hidden", () => {
  it("respects max", () => {
    render(<StarRating max={3} />);
    expect(screen.getAllByRole("button")).toHaveLength(3);
  });
});
""",
            )
        ],
    },
    {
        "slug": "progress-bar-react",
        "title": "Progress Bar (React)",
        "difficulty": "easy",
        "description": "Implement `ProgressBar` — `percent` prop (0–100) sets inner bar width as `percent%`.",
        "language": "react",
        "runtime": "19",
        "starter": """type Props = { percent: number };

export function ProgressBar({ percent }: Props) {
  return (
    <div role="progressbar" aria-valuenow={percent} aria-valuemin={0} aria-valuemax={100}>
      <div className="fill" />
    </div>
  );
}
""",
        "public_tests": [
            (
                "progress.test",
                """import { render } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ProgressBar } from "../solution";

describe("ProgressBar", () => {
  it("sets width from percent", () => {
    const { container } = render(<ProgressBar percent={40} />);
    const fill = container.querySelector(".fill") as HTMLElement;
    expect(fill.style.width).toBe("40%");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "progress_hidden.test",
                """import { render } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ProgressBar } from "../solution";

describe("ProgressBar hidden", () => {
  it("zero percent", () => {
    const { container } = render(<ProgressBar percent={0} />);
    expect((container.querySelector(".fill") as HTMLElement).style.width).toBe("0%");
  });
});
""",
            )
        ],
    },
    {
        "slug": "accordion-react",
        "title": "Accordion (React)",
        "difficulty": "easy",
        "description": "Implement `Accordion` — `items: {title, content}[]`; only one section open at a time; click header toggles.",
        "language": "react",
        "runtime": "19",
        "starter": """export type AccordionItem = { title: string; content: string };

type Props = { items: AccordionItem[] };

export function Accordion({ items }: Props) {
  return <div>{items.map((item) => <div key={item.title}><button type="button">{item.title}</button></div>)}</div>;
}
""",
        "public_tests": [
            (
                "accordion.test",
                """import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Accordion } from "../solution";

describe("Accordion", () => {
  const items = [
    { title: "One", content: "First" },
    { title: "Two", content: "Second" },
  ];

  it("shows content when header clicked", () => {
    render(<Accordion items={items} />);
    fireEvent.click(screen.getByRole("button", { name: "One" }));
    expect(screen.getByText("First")).toBeVisible();
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "accordion_hidden.test",
                """import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Accordion } from "../solution";

describe("Accordion hidden", () => {
  it("closes other panel", () => {
    const items = [
      { title: "A", content: "Alpha" },
      { title: "B", content: "Beta" },
    ];
    render(<Accordion items={items} />);
    fireEvent.click(screen.getByRole("button", { name: "A" }));
    fireEvent.click(screen.getByRole("button", { name: "B" }));
    expect(screen.queryByText("Alpha")).not.toBeVisible();
    expect(screen.getByText("Beta")).toBeVisible();
  });
});
""",
            )
        ],
    },
    {
        "slug": "calculator-react",
        "title": "Calculator Add (React)",
        "difficulty": "easy",
        "description": "Implement `Calculator` — two number inputs and a `Result` span showing their sum.",
        "language": "react",
        "runtime": "19",
        "starter": """import { useState } from "react";

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
""",
        "public_tests": [
            (
                "calc.test",
                """import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Calculator } from "../solution";

describe("Calculator", () => {
  it("sums inputs", () => {
    render(<Calculator />);
    fireEvent.change(screen.getByLabelText("a"), { target: { value: "2" } });
    fireEvent.change(screen.getByLabelText("b"), { target: { value: "3" } });
    expect(screen.getByRole("status")).toHaveTextContent("5");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "calc_hidden.test",
                """import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Calculator } from "../solution";

describe("Calculator hidden", () => {
  it("negative numbers", () => {
    render(<Calculator />);
    fireEvent.change(screen.getByLabelText("a"), { target: { value: "-1" } });
    fireEvent.change(screen.getByLabelText("b"), { target: { value: "1" } });
    expect(screen.getByRole("status")).toHaveTextContent("0");
  });
});
""",
            )
        ],
    },
    {
        "slug": "dark-mode-toggle-react",
        "title": "Dark Mode Toggle (React)",
        "difficulty": "easy",
        "description": "Implement `ThemeToggle` — button toggles `data-theme` on `document.documentElement` between `light` and `dark`.",
        "language": "react",
        "runtime": "19",
        "starter": """export function ThemeToggle() {
  return <button type="button">Toggle theme</button>;
}
""",
        "public_tests": [
            (
                "theme.test",
                """import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ThemeToggle } from "../solution";

describe("ThemeToggle", () => {
  it("toggles data-theme", () => {
    document.documentElement.setAttribute("data-theme", "light");
    render(<ThemeToggle />);
    fireEvent.click(screen.getByRole("button"));
    expect(document.documentElement.getAttribute("data-theme")).toBe("dark");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "theme_hidden.test",
                """import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ThemeToggle } from "../solution";

describe("ThemeToggle hidden", () => {
  it("toggles back to light", () => {
    document.documentElement.setAttribute("data-theme", "dark");
    render(<ThemeToggle />);
    fireEvent.click(screen.getByRole("button"));
    expect(document.documentElement.getAttribute("data-theme")).toBe("light");
  });
});
""",
            )
        ],
    },
    {
        "slug": "searchable-list-react",
        "title": "Searchable List (React)",
        "difficulty": "easy",
        "description": "Implement `SearchableList` — filter `items` by case-insensitive substring match on `query` input.",
        "language": "react",
        "runtime": "19",
        "starter": """type Props = { items: string[] };

export function SearchableList({ items }: Props) {
  return (
    <div>
      <input aria-label="search" />
      <ul>{items.map((i) => <li key={i}>{i}</li>)}</ul>
    </div>
  );
}
""",
        "public_tests": [
            (
                "search.test",
                """import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { SearchableList } from "../solution";

describe("SearchableList", () => {
  it("filters items", () => {
    render(<SearchableList items={["Apple", "Banana", "Apricot"]} />);
    fireEvent.change(screen.getByLabelText("search"), { target: { value: "ap" } });
    expect(screen.getByText("Apple")).toBeInTheDocument();
    expect(screen.getByText("Apricot")).toBeInTheDocument();
    expect(screen.queryByText("Banana")).not.toBeInTheDocument();
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "search_hidden.test",
                """import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { SearchableList } from "../solution";

describe("SearchableList hidden", () => {
  it("empty query shows all", () => {
    render(<SearchableList items={["X", "Y"]} />);
    expect(screen.getByText("X")).toBeInTheDocument();
    expect(screen.getByText("Y")).toBeInTheDocument();
  });
});
""",
            )
        ],
    },
    {
        "slug": "color-box-react",
        "title": "Color Box (React)",
        "difficulty": "easy",
        "description": "Implement `ColorBox` — `color` prop sets background on a div with `role=\"img\"` and `aria-label` equal to color.",
        "language": "react",
        "runtime": "19",
        "starter": """type Props = { color: string };

export function ColorBox({ color }: Props) {
  return <div role="img" aria-label={color} />;
}
""",
        "public_tests": [
            (
                "color.test",
                """import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ColorBox } from "../solution";

describe("ColorBox", () => {
  it("applies background", () => {
    render(<ColorBox color="#ff0000" />);
    const box = screen.getByRole("img");
    expect(box).toHaveStyle({ backgroundColor: "rgb(255, 0, 0)" });
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "color_hidden.test",
                """import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ColorBox } from "../solution";

describe("ColorBox hidden", () => {
  it("named color", () => {
    render(<ColorBox color="blue" />);
    expect(screen.getByRole("img")).toHaveAttribute("aria-label", "blue");
  });
});
""",
            )
        ],
    },
    # --- Vue (vuejs/examples, awesome-vue) ---
    {
        "slug": "hello-vue",
        "title": "Hello Vue",
        "difficulty": "easy",
        "description": "Vue SFC greets `name` prop in an `<h1>` (vuejs/examples hello-world style).",
        "language": "vue",
        "runtime": "3.5",
        "starter": """<script setup lang="ts">
defineProps<{ name: string }>();
</script>

<template>
  <h1>TODO</h1>
</template>
""",
        "public_tests": [
            (
                "hello.test",
                """import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import Hello from "../solution.vue";

describe("Hello", () => {
  it("greets", () => {
    const wrapper = mount(Hello, { props: { name: "Vue" } });
    expect(wrapper.get("h1").text()).toBe("Hello, Vue!");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "hello_hidden.test",
                """import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import Hello from "../solution.vue";

describe("Hello hidden", () => {
  it("another name", () => {
    const wrapper = mount(Hello, { props: { name: "World" } });
    expect(wrapper.get("h1").text()).toBe("Hello, World!");
  });
});
""",
            )
        ],
    },
    {
        "slug": "computed-filter-vue",
        "title": "Computed Filter (Vue)",
        "difficulty": "easy",
        "description": "SFC `ItemFilter` — prop `items: string[]`, input filters list case-insensitively (computed pattern).",
        "language": "vue",
        "runtime": "3.5",
        "starter": """<script setup lang="ts">
import { ref } from "vue";

const props = defineProps<{ items: string[] }>();
const query = ref("");
</script>

<template>
  <input v-model="query" aria-label="filter" />
  <ul>
    <li v-for="item in props.items" :key="item">{{ item }}</li>
  </ul>
</template>
""",
        "public_tests": [
            (
                "filter.test",
                """import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import ItemFilter from "../solution.vue";

describe("ItemFilter", () => {
  it("filters", async () => {
    const wrapper = mount(ItemFilter, { props: { items: ["Cat", "Dog", "Caterpillar"] } });
    await wrapper.get("[aria-label=filter]").setValue("cat");
    expect(wrapper.text()).toContain("Cat");
    expect(wrapper.text()).toContain("Caterpillar");
    expect(wrapper.text()).not.toContain("Dog");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "filter_hidden.test",
                """import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import ItemFilter from "../solution.vue";

describe("ItemFilter hidden", () => {
  it("shows all when empty", () => {
    const wrapper = mount(ItemFilter, { props: { items: ["A", "B"] } });
    expect(wrapper.text()).toContain("A");
    expect(wrapper.text()).toContain("B");
  });
});
""",
            )
        ],
    },
    {
        "slug": "todo-list-vue",
        "title": "Todo List (Vue)",
        "difficulty": "medium",
        "description": "SFC `TodoList` — add todo on button click; list renders todo text.",
        "language": "vue",
        "runtime": "3.5",
        "starter": """<script setup lang="ts">
import { ref } from "vue";

const text = ref("");
const todos = ref<string[]>([]);

function addTodo() {
  throw new Error("TODO");
}
</script>

<template>
  <input v-model="text" aria-label="todo" />
  <button type="button" @click="addTodo">Add</button>
  <ul><li v-for="(t, i) in todos" :key="i">{{ t }}</li></ul>
</template>
""",
        "public_tests": [
            (
                "todo_vue.test",
                """import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import TodoList from "../solution.vue";

describe("TodoList", () => {
  it("adds todo", async () => {
    const wrapper = mount(TodoList);
    await wrapper.get("[aria-label=todo]").setValue("Learn Vue");
    await wrapper.get("button").trigger("click");
    expect(wrapper.text()).toContain("Learn Vue");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "todo_vue_hidden.test",
                """import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import TodoList from "../solution.vue";

describe("TodoList hidden", () => {
  it("adds two", async () => {
    const wrapper = mount(TodoList);
    await wrapper.get("[aria-label=todo]").setValue("A");
    await wrapper.get("button").trigger("click");
    await wrapper.get("[aria-label=todo]").setValue("B");
    await wrapper.get("button").trigger("click");
    expect(wrapper.findAll("li")).toHaveLength(2);
  });
});
""",
            )
        ],
    },
    {
        "slug": "emit-counter-vue",
        "title": "Emit Counter (Vue)",
        "difficulty": "easy",
        "description": "SFC `CounterPanel` — button click increments count shown in `aria-live` region (emit pattern in same file).",
        "language": "vue",
        "runtime": "3.5",
        "starter": """<script setup lang="ts">
import { ref } from "vue";

const count = ref(0);

function increment() {
  throw new Error("TODO");
}
</script>

<template>
  <p aria-live="polite">{{ count }}</p>
  <button type="button" @click="increment">+1</button>
</template>
""",
        "public_tests": [
            (
                "emit.test",
                """import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import CounterPanel from "../solution.vue";

describe("CounterPanel", () => {
  it("increments", async () => {
    const wrapper = mount(CounterPanel);
    await wrapper.get("button").trigger("click");
    expect(wrapper.get("[aria-live=polite]").text()).toBe("1");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "emit_hidden.test",
                """import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import CounterPanel from "../solution.vue";

describe("CounterPanel hidden", () => {
  it("double click", async () => {
    const wrapper = mount(CounterPanel);
    const btn = wrapper.get("button");
    await btn.trigger("click");
    await btn.trigger("click");
    expect(wrapper.get("[aria-live=polite]").text()).toBe("2");
  });
});
""",
            )
        ],
    },
    # --- Angular (learning-zone/angular-basics, awesome-angular, RealWorld-style) ---
    {
        "slug": "truncate-pipe-angular",
        "title": "Truncate Pipe (Angular)",
        "difficulty": "easy",
        "description": "Standalone `TruncatePipe` — `transform(value, limit)` returns value if short else `value.slice(0, limit) + '...'`.",
        "language": "angular",
        "runtime": "19",
        "starter": """import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: "truncate", standalone: true })
export class TruncatePipe implements PipeTransform {
  transform(value: string, limit: number): string {
    throw new Error("TODO");
  }
}
""",
        "public_tests": [
            (
                "truncate.test",
                """import { describe, expect, it } from "vitest";
import { TruncatePipe } from "../solution";

describe("TruncatePipe", () => {
  it("truncates", () => {
    const pipe = new TruncatePipe();
    expect(pipe.transform("Hello World", 5)).toBe("Hello...");
  });

  it("short text", () => {
    const pipe = new TruncatePipe();
    expect(pipe.transform("Hi", 5)).toBe("Hi");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "truncate_hidden.test",
                """import { describe, expect, it } from "vitest";
import { TruncatePipe } from "../solution";

describe("TruncatePipe hidden", () => {
  it("exact limit", () => {
    const pipe = new TruncatePipe();
    expect(pipe.transform("abcde", 5)).toBe("abcde");
  });
});
""",
            )
        ],
    },
    {
        "slug": "title-case-pipe-angular",
        "title": "Title Case Pipe (Angular)",
        "difficulty": "easy",
        "description": "Standalone `TitleCasePipe` — capitalizes first letter of each word.",
        "language": "angular",
        "runtime": "19",
        "starter": """import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: "titleCase", standalone: true })
export class TitleCasePipe implements PipeTransform {
  transform(value: string): string {
    throw new Error("TODO");
  }
}
""",
        "public_tests": [
            (
                "titlecase.test",
                """import { describe, expect, it } from "vitest";
import { TitleCasePipe } from "../solution";

describe("TitleCasePipe", () => {
  it("title cases", () => {
    const pipe = new TitleCasePipe();
    expect(pipe.transform("hello world")).toBe("Hello World");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "titlecase_hidden.test",
                """import { describe, expect, it } from "vitest";
import { TitleCasePipe } from "../solution";

describe("TitleCasePipe hidden", () => {
  it("single word", () => {
    const pipe = new TitleCasePipe();
    expect(pipe.transform("angular")).toBe("Angular");
  });
});
""",
            )
        ],
    },
    {
        "slug": "initials-pipe-angular",
        "title": "Initials Pipe (Angular)",
        "difficulty": "easy",
        "description": "Standalone `InitialsPipe` — `John Doe` → `JD`; single name → first two letters uppercased.",
        "language": "angular",
        "runtime": "19",
        "starter": """import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: "initials", standalone: true })
export class InitialsPipe implements PipeTransform {
  transform(fullName: string): string {
    throw new Error("TODO");
  }
}
""",
        "public_tests": [
            (
                "initials.test",
                """import { describe, expect, it } from "vitest";
import { InitialsPipe } from "../solution";

describe("InitialsPipe", () => {
  it("two names", () => {
    const pipe = new InitialsPipe();
    expect(pipe.transform("John Doe")).toBe("JD");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "initials_hidden.test",
                """import { describe, expect, it } from "vitest";
import { InitialsPipe } from "../solution";

describe("InitialsPipe hidden", () => {
  it("single name", () => {
    const pipe = new InitialsPipe();
    expect(pipe.transform("Ada")).toBe("AD");
  });
});
""",
            )
        ],
    },
    {
        "slug": "multiply-pipe-angular",
        "title": "Multiply Pipe (Angular)",
        "difficulty": "easy",
        "description": "Standalone `MultiplyPipe` — `transform(value, factor)` returns numeric product.",
        "language": "angular",
        "runtime": "19",
        "starter": """import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: "multiply", standalone: true })
export class MultiplyPipe implements PipeTransform {
  transform(value: number, factor: number): number {
    throw new Error("TODO");
  }
}
""",
        "public_tests": [
            (
                "multiply.test",
                """import { describe, expect, it } from "vitest";
import { MultiplyPipe } from "../solution";

describe("MultiplyPipe", () => {
  it("multiplies", () => {
    const pipe = new MultiplyPipe();
    expect(pipe.transform(3, 4)).toBe(12);
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "multiply_hidden.test",
                """import { describe, expect, it } from "vitest";
import { MultiplyPipe } from "../solution";

describe("MultiplyPipe hidden", () => {
  it("zero factor", () => {
    const pipe = new MultiplyPipe();
    expect(pipe.transform(9, 0)).toBe(0);
  });
});
""",
            )
        ],
    },
    {
        "slug": "slugify-service-angular",
        "title": "Slugify Service (Angular)",
        "difficulty": "easy",
        "description": "`SlugifyService.slugify(text)` — lowercase, spaces to hyphens, strip non-alphanumeric (RealWorld-style slug).",
        "language": "angular",
        "runtime": "19",
        "starter": """import { Injectable } from "@angular/core";

@Injectable({ providedIn: "root" })
export class SlugifyService {
  slugify(text: string): string {
    throw new Error("TODO");
  }
}
""",
        "public_tests": [
            (
                "slugify.test",
                """import { describe, expect, it } from "vitest";
import { SlugifyService } from "../solution";

describe("SlugifyService", () => {
  it("slugifies", () => {
    const svc = new SlugifyService();
    expect(svc.slugify("Hello World!")).toBe("hello-world");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "slugify_hidden.test",
                """import { describe, expect, it } from "vitest";
import { SlugifyService } from "../solution";

describe("SlugifyService hidden", () => {
  it("trims hyphens", () => {
    const svc = new SlugifyService();
    expect(svc.slugify("  A--B  ")).toBe("a-b");
  });
});
""",
            )
        ],
    },
]
