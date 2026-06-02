# Challenge catalog

All **11 language tracks** are runnable with Docker runners. Full source list: [challenge-catalog-sources.md](challenge-catalog-sources.md).

**How to add challenges:** [adding-challenges.md](./adding-challenges.md) (seed, admin UI, manual tree, future paths).

| Language | Runner | Seed modules | ~Count |
|----------|--------|--------------|--------|
| Java | `runners/java/` | `catalog.py` | 28+ |
| Python | `runners/python/` | `catalog.py` | 16+ |
| Go | `runners/go/` | `catalog_multi.py`, `catalog_multi_extended.py` | 22 |
| Node.js | `runners/node/` | same | 22 |
| TypeScript | `runners/typescript/` | multi + extended + `catalog_typescript_extra.py` | 42 |
| C# | `runners/dotnet/` | multi + extended | 22 |
| Rust | `runners/rust/` | multi + extended | 22 |
| C++ | `runners/cpp/` | multi + extended | 22 |
| React | `runners/react/` | `catalog_frontend.py`, `catalog_frontend_extra.py` | 11 |
| Vue | `runners/vue/` | same | 6 |
| Angular | `runners/angular/` | same | 9 |

Regenerate:

```bash
python3 scripts/seed-challenges/generate.py          # new slugs only
python3 scripts/seed-challenges/generate.py --force  # overwrite all
```

Restart the API after seeding so `ChallengeGitLoader` imports new slugs.

## Recently added (inspiration-backed)

- **12 algorithms × 6 langs** — two-sum, valid-parentheses, binary-search, reverse-string, valid-palindrome, max-subarray, single-number, plus-one, best-time-stock, merge-sorted-arrays, bubble-sort, anagram-check (`catalog_multi_extended.py`)
- **10 TypeScript utilities** — capitalize, chunk, flatten, omit, pick, unique, camelCase, truncate, shallowEqual, groupBy (`catalog_typescript_extra.py`)
- **17 frontend exercises** — React app-ideas set, Vue examples, Angular pipes/services (`catalog_frontend_extra.py`)

## Future enhancements

Detailed workflows for each item: [adding-challenges.md#future-authoring](./adding-challenges.md#future-authoring).

| Enhancement | Notes |
| --- | --- |
| Type-only challenges | [type-challenges](https://github.com/type-challenges/type-challenges) — compile checker in TS runner |
| Multi-file / full-repo | [project-based-learning](https://github.com/practical-tutorials/project-based-learning), [eShop](https://github.com/dotnet-architecture/eShop) — workspace + runner layout TBD |
| Angular `TestBed` | Component tests; heavier runner image |
| [greatfrontend](https://github.com/greatfrontend/greatfrontend-projects) | Widget clones (data table, modal) |
| AI-generated challenges | BYO key; must pass review + green CI before publish |
| External repo import | Converter script → `challenges/{slug}/` + PR review |

When implementing any of the above, update [adding-challenges.md](./adding-challenges.md) and [contracts.md](./contracts.md) in the same PR.
