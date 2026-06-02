"""Frontend framework challenges (React, Vue, Angular) — app-ideas / awesome-vue inspired."""

FRONTEND_CHALLENGES = [
    {
        "slug": "counter-react",
        "title": "Counter (React)",
        "difficulty": "easy",
        "description": "Implement `Counter` — a button showing `initial` count; each click increments by 1.",
        "language": "react",
        "runtime": "19",
        "starter": """import { useState } from "react";

type CounterProps = {
  initial?: number;
};

export function Counter({ initial = 0 }: CounterProps) {
  const [count] = useState(initial);
  return <button type="button">{count}</button>;
}
""",
        "public_tests": [
            (
                "counter.test",
                """import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Counter } from "../solution";

describe("Counter", () => {
  it("shows initial count", () => {
    render(<Counter initial={3} />);
    expect(screen.getByRole("button")).toHaveTextContent("3");
  });

  it("increments on click", () => {
    render(<Counter initial={0} />);
    fireEvent.click(screen.getByRole("button"));
    expect(screen.getByRole("button")).toHaveTextContent("1");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "counter_hidden.test",
                """import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Counter } from "../solution";

describe("Counter hidden", () => {
  it("increments twice from 5", () => {
    render(<Counter initial={5} />);
    const btn = screen.getByRole("button");
    fireEvent.click(btn);
    fireEvent.click(btn);
    expect(btn).toHaveTextContent("7");
  });
});
""",
            )
        ],
    },
    {
        "slug": "greeting-react",
        "title": "Greeting (React)",
        "difficulty": "easy",
        "description": "Implement `Greeting` — render `Hello, {name}!` in a heading.",
        "language": "react",
        "runtime": "19",
        "starter": """type GreetingProps = {
  name: string;
};

export function Greeting({ name }: GreetingProps) {
  return <h1>TODO</h1>;
}
""",
        "public_tests": [
            (
                "greeting.test",
                """import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Greeting } from "../solution";

describe("Greeting", () => {
  it("renders name", () => {
    render(<Greeting name="Ada" />);
    expect(screen.getByRole("heading")).toHaveTextContent("Hello, Ada!");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "greeting_hidden.test",
                """import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Greeting } from "../solution";

describe("Greeting hidden", () => {
  it("handles another name", () => {
    render(<Greeting name="Grace" />);
    expect(screen.getByRole("heading")).toHaveTextContent("Hello, Grace!");
  });
});
""",
            )
        ],
    },
    {
        "slug": "counter-vue",
        "title": "Counter (Vue)",
        "difficulty": "easy",
        "description": "Implement a Vue SFC `Counter` with prop `initial` (default 0) and a button that increments.",
        "language": "vue",
        "runtime": "3.5",
        "starter": """<script setup lang="ts">
import { ref } from "vue";

const props = withDefaults(defineProps<{ initial?: number }>(), { initial: 0 });
const count = ref(props.initial);

function increment() {
  throw new Error("TODO");
}
</script>

<template>
  <button type="button" @click="increment">{{ count }}</button>
</template>
""",
        "public_tests": [
            (
                "counter.test",
                """import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import Counter from "../solution.vue";

describe("Counter", () => {
  it("shows initial", () => {
    const wrapper = mount(Counter, { props: { initial: 4 } });
    expect(wrapper.get("button").text()).toBe("4");
  });

  it("increments", async () => {
    const wrapper = mount(Counter, { props: { initial: 0 } });
    await wrapper.get("button").trigger("click");
    expect(wrapper.get("button").text()).toBe("1");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "counter_hidden.test",
                """import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import Counter from "../solution.vue";

describe("Counter hidden", () => {
  it("double increment", async () => {
    const wrapper = mount(Counter, { props: { initial: 2 } });
    const btn = wrapper.get("button");
    await btn.trigger("click");
    await btn.trigger("click");
    expect(btn.text()).toBe("4");
  });
});
""",
            )
        ],
    },
    {
        "slug": "reverse-pipe-angular",
        "title": "Reverse Pipe (Angular)",
        "difficulty": "easy",
        "description": "Implement standalone `ReversePipe` — reverses a string (empty string stays empty).",
        "language": "angular",
        "runtime": "19",
        "starter": """import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: "reverse", standalone: true })
export class ReversePipe implements PipeTransform {
  transform(value: string): string {
    throw new Error("TODO");
  }
}
""",
        "public_tests": [
            (
                "reverse.test",
                """import { describe, expect, it } from "vitest";
import { ReversePipe } from "../solution";

describe("ReversePipe", () => {
  it("reverses text", () => {
    const pipe = new ReversePipe();
    expect(pipe.transform("abc")).toBe("cba");
  });

  it("empty string", () => {
    const pipe = new ReversePipe();
    expect(pipe.transform("")).toBe("");
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "reverse_hidden.test",
                """import { describe, expect, it } from "vitest";
import { ReversePipe } from "../solution";

describe("ReversePipe hidden", () => {
  it("palindrome", () => {
    const pipe = new ReversePipe();
    expect(pipe.transform("level")).toBe("level");
  });
});
""",
            )
        ],
    },
    {
        "slug": "double-service-angular",
        "title": "Double Service (Angular)",
        "difficulty": "easy",
        "description": "Implement `DoubleService` with method `double(n: number): number`.",
        "language": "angular",
        "runtime": "19",
        "starter": """import { Injectable } from "@angular/core";

@Injectable({ providedIn: "root" })
export class DoubleService {
  double(n: number): number {
    throw new Error("TODO");
  }
}
""",
        "public_tests": [
            (
                "double.test",
                """import { describe, expect, it } from "vitest";
import { DoubleService } from "../solution";

describe("DoubleService", () => {
  it("doubles", () => {
    const svc = new DoubleService();
    expect(svc.double(3)).toBe(6);
  });

  it("zero", () => {
    const svc = new DoubleService();
    expect(svc.double(0)).toBe(0);
  });
});
""",
            )
        ],
        "hidden_tests": [
            (
                "double_hidden.test",
                """import { describe, expect, it } from "vitest";
import { DoubleService } from "../solution";

describe("DoubleService hidden", () => {
  it("negative", () => {
    const svc = new DoubleService();
    expect(svc.double(-4)).toBe(-8);
  });
});
""",
            )
        ],
    },
]
