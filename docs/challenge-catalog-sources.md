# Challenge catalog — inspiration sources

The seeded catalog is **inspired by** (not a direct copy of) exercises and algorithms from the repositories below. Each challenge in `challenges/{slug}/` is an **original on-disk package**: starter code, public/hidden tests, and a short `description_md` written for this platform.

Regenerate from seed modules:

```bash
python3 scripts/seed-challenges/generate.py        # new slugs only
python3 scripts/seed-challenges/generate.py --force # overwrite all challenge.yml trees
```

Restart the API after seeding so `ChallengeGitLoader` syncs metadata into Postgres.

---

## Primary inspiration (your catalog)

| Repository | Used for |
| --- | --- |
| [TheAlgorithms/Java](https://github.com/TheAlgorithms/Java) | Java DSA exercises — `scripts/seed-challenges/catalog.py` |
| [TheAlgorithms/Python](https://github.com/TheAlgorithms/Python) | Python DSA — `catalog.py` |
| [TheAlgorithms/Go](https://github.com/TheAlgorithms/Go) | Go — `catalog_multi.py`, `catalog_multi_extended.py` |
| [TheAlgorithms/C-Sharp](https://github.com/TheAlgorithms/C-Sharp) | C# — `catalog_multi.py`, `catalog_multi_extended.py` |
| [TheAlgorithms/Rust](https://github.com/TheAlgorithms/Rust) | Rust — `catalog_multi.py`, `catalog_multi_extended.py` |
| [TheAlgorithms/C-Plus-Plus](https://github.com/TheAlgorithms/C-Plus-Plus) | C++ — `catalog_multi.py`, `catalog_multi_extended.py` |
| [TheAlgorithms/TypeScript](https://github.com/TheAlgorithms/TypeScript) | TypeScript DSA — `catalog_multi.py`, `catalog_multi_extended.py`, `catalog_typescript_extra.py` |
| [type-challenges/type-challenges](https://github.com/type-challenges/type-challenges) | TypeScript utility-style exercises (runtime ports; type-only challenges are backlog) |
| [florinpop17/app-ideas](https://github.com/florinpop17/app-ideas) | React / Vue UI component ideas — `catalog_frontend.py`, `catalog_frontend_extra.py` |
| [gothinkster/realworld](https://github.com/gothinkster/realworld) | Angular services/pipes patterns (e.g. slugify) — `catalog_frontend.py`, `catalog_frontend_extra.py` |

**Also referenced in seed headers:** [kunal-kushwaha/DSA-Bootcamp-Java](https://github.com/kunal-kushwaha/DSA-Bootcamp-Java), [Asabeneh/30-Days-Of-Python](https://github.com/Asabeneh/30-Days-Of-Python), [project-based-learning](https://github.com/practical-tutorials/project-based-learning).

### Why some exercises feel short on context

Upstream repos focus on **implementations and unit tests**, not learner-facing problem statements. The generator turns each entry into:

- A one-line **task** in `description_md` (method signature + goal)
- **Examples** (from public test assertions when you run `generate.py`)
- Runnable **starter** + **public** / **hidden** tests

To add more explanation (story, constraints, edge cases), enrich `description_md` in `challenge.yml` or extend the catalog entry’s `description` field before regenerating — see [adding-challenges.md](./adding-challenges.md#input--output-examples-learner-facing).

---

## Java

| Source | Role in catalog |
| --- | --- |
| [TheAlgorithms/Java](https://github.com/TheAlgorithms/Java) | Core DSA slugs (factorial, two-sum-style ports, stock, trees, etc.) |
| [kunal-kushwaha/DSA-Bootcamp-Java](https://github.com/kunal-kushwaha/DSA-Bootcamp-Java) | Additional classic problems |

**Seed:** `scripts/seed-challenges/catalog.py`

---

## Python

| Source | Role in catalog |
| --- | --- |
| [TheAlgorithms/Python](https://github.com/TheAlgorithms/Python) | Core DSA |
| [Asabeneh/30-Days-Of-Python](https://github.com/Asabeneh/30-Days-Of-Python) | Beginner-friendly variants |

**Seed:** `scripts/seed-challenges/catalog.py`

---

## Go

| Source | Role in catalog |
| --- | --- |
| [TheAlgorithms/Go](https://github.com/TheAlgorithms/Go) | DSA + extended algorithms |
| [golang/go/wiki/CodeReviewComments](https://github.com/golang/go/wiki/CodeReviewComments) | Idiomatic naming in generated tests |

**Seed:** `catalog_multi.py`, `catalog_multi_extended.py`

---

## Node.js

| Source | Role in catalog |
| --- | --- |
| [TheAlgorithms/JavaScript](https://github.com/TheAlgorithms/JavaScript) | Same DSA themes as multi-language extended set (ported to `node:test`) |

**Seed:** `catalog_multi.py`, `catalog_multi_extended.py` (language key `node`)

---

## TypeScript

| Source | Role in catalog |
| --- | --- |
| [type-challenges/type-challenges](https://github.com/type-challenges/type-challenges) | Utility exercises (Pick/Omit-style at runtime; compile-only TBD) |
| [TheAlgorithms/TypeScript](https://github.com/TheAlgorithms/TypeScript) | DSA ports |
| [TypeScript handbook](https://github.com/microsoft/TypeScript-Website/tree/v2/packages/typescriptlang-org/src/copy/en/handbook-v2) | `capitalize`, `groupBy`, shallow compare |

**Seed:** `catalog_multi.py`, `catalog_multi_extended.py`, `catalog_typescript_extra.py`

---

## C#

| Source | Role in catalog |
| --- | --- |
| [TheAlgorithms/C-Sharp](https://github.com/TheAlgorithms/C-Sharp) | Classic DSA in multi + extended modules |

**Seed:** `catalog_multi.py`, `catalog_multi_extended.py`

---

## Rust

| Source | Role in catalog |
| --- | --- |
| [TheAlgorithms/Rust](https://github.com/TheAlgorithms/Rust) | DSA ports |
| [rust-lang/rustlings](https://github.com/rust-lang/rustlings) | Small `assert_eq!` exercise style |

**Seed:** `catalog_multi.py`, `catalog_multi_extended.py`

---

## C++

| Source | Role in catalog |
| --- | --- |
| [TheAlgorithms/C-Plus-Plus](https://github.com/TheAlgorithms/C-Plus-Plus) | DSA ports |

**Seed:** `catalog_multi.py`, `catalog_multi_extended.py`

---

## React

| Source | Role in catalog |
| --- | --- |
| [florinpop17/app-ideas](https://github.com/florinpop17/app-ideas) | Counter, greeting, todo, calculator, progress, star rating, accordion, dark mode, searchable list, color box |
| [greatfrontend/greatfrontend-projects](https://github.com/greatfrontend/greatfrontend-projects) | Component + interaction patterns |

**Seed:** `catalog_frontend.py`, `catalog_frontend_extra.py`

---

## Vue

| Source | Role in catalog |
| --- | --- |
| [florinpop17/app-ideas](https://github.com/florinpop17/app-ideas) | Counter, todos |
| [vuejs/examples](https://github.com/vuejs/examples) | Hello world, computed/filter, emits |

**Seed:** `catalog_frontend.py`, `catalog_frontend_extra.py`

---

## SQL (PostgreSQL)

| Source | Role in catalog |
| --- | --- |
| [PostgreSQL Exercises](https://pgexercises.com/) | Schema and drill patterns (employees / departments) |
| [SQLBolt](https://sqlbolt.com/) | Beginner-friendly SELECT / WHERE / JOIN progression |
| [Select Star SQL](https://selectstarsql.com/) | Narrative query practice ideas |

**Seed:** `scripts/seed-challenges/catalog_sql.py` — 10 original challenges with `setup/schema.sql` and pytest result-set checks.

**Runner:** `runners/sql/` — embedded PostgreSQL 17, `workspace_layout: postgres-sql`, no line coverage gate.

---

## Angular

| Source | Role in catalog |
| --- | --- |
| [gothinkster/realworld](https://github.com/gothinkster/realworld) | Slugify service (article slugs) |
| [learning-zone/angular-basics](https://github.com/learning-zone/angular-basics) | Pipes (reverse, truncate, title case) |

**Seed:** `catalog_frontend.py`, `catalog_frontend_extra.py`

---

## Seed module index

| File | Contents |
| --- | --- |
| `catalog.py` | Java + Python |
| `catalog_multi.py` | Go, Node, TS, C#, Rust, C++ (core DSA) |
| `catalog_multi_extended.py` | +12 algorithms × 6 languages |
| `catalog_typescript_extra.py` | TS utility challenges |
| `catalog_frontend.py` | Base React / Vue / Angular |
| `catalog_frontend_extra.py` | app-ideas / RealWorld-style UI |
| `catalog_sql.py` | PostgreSQL SQL drills |

---

## Enriching exercises for learners

`scripts/seed-challenges/challenge_enrichment.py` builds structured `description_md` when you regenerate:

| Section | Content |
| --- | --- |
| **What to do** | Catalog task line + method signature |
| *(context)* | Slug-specific narrative (stock, anagram, UI tracks, …) |
| **Examples** | Parsed from public test assertions |
| **Constraints** | Platform rules + language notes |
| **Method to implement** | Extracted signature |

```bash
python3 scripts/seed-challenges/generate.py --force
```

Restart the API — `ChallengeGitLoader` syncs updated `description_md` into Postgres for existing slugs.

| Approach | When to use |
| --- | --- |
| `generate.py --force` | Bulk refresh all catalog challenges |
| Edit `challenges/{slug}/challenge.yml` | One-off overrides |
| Extend `SLUG_CONTEXT` in `challenge_enrichment.py` | Add narrative for a family of problems |
| Admin **Create challenge** | Custom exercises with full markdown up front |

Examples appear in `description_md` in the workspace **Problem** panel — see [adding-challenges.md](./adding-challenges.md).
