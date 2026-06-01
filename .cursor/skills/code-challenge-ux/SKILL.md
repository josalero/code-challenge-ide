---
name: code-challenge-ux
description: >-
  Design and implement the Code Training Lab coding workspace — desktop-first
  split layout, Monaco editor, run/submit flows, bottom output tabs, and all
  run lifecycle states. Use when building or changing ChallengeWorkspacePage
  or workspace components.
---

# Code Challenge UX

## Layout (desktop-first)

```
┌ Header ─────────────────────────────────────────────────────────┐
│ Title · language · runtime version · autosave · Run · Submit    │
├ Instructions (left) ──┬── Monaco editor (right) ────────────────┤
│ Problem markdown      │ Solution / custom tests tabs          │
│ Public test names     │                                       │
├───────────────────────┴───────────────────────────────────────┤
│ Bottom panel tabs (resizable height)                          │
│ Tests | Compiler | Static analysis | Feedback | History       │
└───────────────────────────────────────────────────────────────┘
```

- **Left:** problem instructions, difficulty, public test overview.
- **Right:** Monaco editor (solution + optional custom tests tab).
- **Horizontal split:** resizable via `ResizablePanelGroup` (default ~38% / 62%).
- **Vertical split:** editor area + bottom output panel (default ~65% / 35%).

## Responsive (< lg / 1024px)

- Do **not** squeeze split panes on narrow viewports.
- **Top tabs:** Instructions | Editor (full width each).
- **Output:** bottom `Sheet` or tab strip — never a third cramped column.
- **Instructions drawer:** `Sheet` from left, triggered by "View problem" button.

## Actions

| Action | Role | Keyboard |
| --- | --- | --- |
| **Run tests** | Secondary — execute against Docker runner | ⌘/Ctrl + Enter |
| **Submit** | Primary — same pipeline, emphasized as final attempt | — |
| **Cancel run** | Visible while PENDING/RUNNING | — |

Both Run and Submit call the submission API today; distinguish visually for future split.

## Autosave

- Debounced localStorage draft per `slug` (800 ms).
- Status line: `Saving…` → `Saved locally` → `Save failed` (never silent).
- Custom tests: explicit save + debounced PUT when tab active.

## Bottom tabs

| Tab | Content |
| --- | --- |
| **Test results** | Live tracked tests + pass/fail summary |
| **Compiler output** | stdout/stderr from runner |
| **Static analysis** | STYLE / coverage linter feedback from report |
| **Evaluator feedback** | AI coach panel + category filters |
| **Attempt history** | Session submissions for this challenge |

Auto-focus tab on state change:
- `compilation-error` → Compiler output
- `failed-test` → Test results
- `successful-submission` → Evaluator feedback

## Run phases (UI states)

Implement all visibly:

| Phase | Banner / empty state |
| --- | --- |
| `loading` | Skeleton layout |
| `running` | Progress steps + live stream badge |
| `compilation-error` | Destructive alert + stderr |
| `failed-test` | Warning alert + failing test list |
| `timeout` | Destructive alert, retry CTA |
| `service-unavailable` | Destructive alert, check infra hint |
| `successful-submission` | Success alert + stats |
| `idle` | "Press Run or ⌘/Ctrl+Enter" hint |

## Components

Prefer **shadcn/ui** (`Button`, `Select`, `Tabs`, `Badge`, `Alert`, `Resizable`, `Sheet`, `ScrollArea`, `Skeleton`, `Tooltip`).

Keep **Ant Design** only where already wired (AppLayout header, AiCoachPanel internals) until migrated.

## Accessibility

- Semantic regions: `main`, `aside`, `nav`, `section` with `aria-label`.
- All toolbar controls: visible labels or `aria-label`.
- Bottom tabs: roving tabindex, arrow keys via Radix Tabs.
- Resizable handles: keyboard focusable, `aria-orientation`.
- Status banners: `role="status"` / `aria-live="polite"`.
- Skip link preserved from AppLayout.

## Visual

- Match existing CTL tokens: slate-950 bg, emerald-400 accent.
- shadcn `dark` class on workspace root.
- Editor frame: `ctl-editor-frame` border + inset shadow.
