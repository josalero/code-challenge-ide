# Challenge catalog — inspiration sources

Use these references when adding or reviewing challenges. Regenerate with:

```bash
python3 scripts/seed-challenges/generate.py        # new slugs only
python3 scripts/seed-challenges/generate.py --force # overwrite all
```

## React

| Source | Ideas mapped in catalog |
|--------|-------------------------|
| [florinpop17/app-ideas](https://github.com/florinpop17/app-ideas) | Counter, greeting, todo, calculator, progress, star rating, accordion, dark mode, searchable list, color box |
| [greatfrontend/greatfrontend-projects](https://github.com/greatfrontend/greatfrontend-projects) | Component + interaction patterns (lists, toggles, ratings) |
| [alan2207/bulletproof-react](https://github.com/alan2207/bulletproof-react) | Small feature-style components, testable units |

**Seed:** `catalog_frontend.py`, `catalog_frontend_extra.py`

## Vue

| Source | Ideas mapped |
|--------|----------------|
| [florinpop17/app-ideas](https://github.com/florinpop17/app-ideas) | Counter, todos |
| [vuejs/examples](https://github.com/vuejs/examples) | Hello world, computed/filter, emits |
| [vuejs/awesome-vue](https://github.com/vuejs/awesome-vue) | SFC + composition API patterns |

**Seed:** `catalog_frontend.py`, `catalog_frontend_extra.py`

## Angular

| Source | Ideas mapped |
|--------|----------------|
| [gothinkster/realworld](https://github.com/gothinkster/realworld) | Slugify service (article slugs) |
| [learning-zone/angular-basics](https://github.com/learning-zone/angular-basics) | Pipes (reverse, truncate, title case, multiply) |
| [PatrickJS/awesome-angular](https://github.com/PatrickJS/awesome-angular) | Standalone pipes/services |

**Seed:** `catalog_frontend.py`, `catalog_frontend_extra.py`

## TypeScript

| Source | Ideas mapped |
|--------|----------------|
| [type-challenges/type-challenges](https://github.com/type-challenges/type-challenges) | Pick/omit-style utilities (runtime exercises; type-only TBD) |
| [TheAlgorithms/TypeScript](https://github.com/TheAlgorithms/TypeScript) | DSA ports via `catalog_multi.py` + extras |
| [TypeScript handbook v2](https://github.com/microsoft/TypeScript-Website/tree/v2/packages/typescriptlang-org/src/copy/en/handbook-v2) | `capitalize`, `groupBy`, shallow compare |

**Seed:** `catalog_multi.py`, `catalog_multi_extended.py`, `catalog_typescript_extra.py`

## C#

| Source | Ideas mapped |
|--------|----------------|
| [TheAlgorithms/C-Sharp](https://github.com/TheAlgorithms/C-Sharp) | Classic DSA in `catalog_multi.py` / `catalog_multi_extended.py` |
| [ardalis/CleanArchitecture](https://github.com/ardalis/CleanArchitecture) | Future: layered service exercises |
| [dotnet-architecture/eShop](https://github.com/dotnet-architecture/eShop) | Future: domain-style modules |

## Go

| Source | Ideas mapped |
|--------|----------------|
| [TheAlgorithms/Go](https://github.com/TheAlgorithms/Go) | DSA + extended algorithms |
| [golang/go/wiki/CodeReviewComments](https://github.com/golang/go/wiki/CodeReviewComments) | Idiomatic naming in generated tests |
| [avelino/awesome-go](https://github.com/avelino/awesome-go) | Standard library style functions |

**Seed:** `catalog_multi.py`, `catalog_multi_extended.py`

## Rust

| Source | Ideas mapped |
|--------|----------------|
| [rust-lang/rustlings](https://github.com/rust-lang/rustlings) | Small `assert_eq!` exercises |
| [TheAlgorithms/Rust](https://github.com/TheAlgorithms/Rust) | DSA ports |
| [rust-unofficial/awesome-rust](https://github.com/rust-unofficial/awesome-rust) | Ownership-friendly APIs |

**Seed:** `catalog_multi.py`, `catalog_multi_extended.py`

## C++

| Source | Ideas mapped |
|--------|----------------|
| [TheAlgorithms/C-Plus-Plus](https://github.com/TheAlgorithms/C-Plus-Plus) | DSA ports |
| [isocpp/CppCoreGuidelines](https://github.com/isocpp/CppCoreGuidelines) | `const` refs, vectors in signatures |
| [practical-tutorials/project-based-learning](https://github.com/practical-tutorials/project-based-learning) | Future: multi-file projects |

**Seed:** `catalog_multi.py`, `catalog_multi_extended.py`

## Module index

| File | Contents |
|------|----------|
| `catalog.py` | Java + Python |
| `catalog_multi.py` | Go, Node, TS, C#, Rust, C++ (core DSA) |
| `catalog_multi_extended.py` | +12 algorithms × 6 languages |
| `catalog_typescript_extra.py` | TS utility challenges |
| `catalog_frontend.py` | Base React/Vue/Angular |
| `catalog_frontend_extra.py` | App-ideas / examples / RealWorld-style UI |
