---
name: react-web
description: Conventions and rules for writing frontend code in apps/web (React 19, TypeScript, Vite, wouter, TanStack Query, axios). Use whenever you add or change a page, component, hook, API call, type or route in apps/web, or work on the prototype's UI/UX.
---

# React web (`apps/web`)

UI/UX is worth **10 of 60 points**, and the video (10 pts) is recorded from this very interface. So every screen is: fast, clear at first glance, with loading, error and empty states handled. Code must stay consistent with the existing skeleton.

## Stack (verified in `package.json`)
- React 19, TypeScript (strict), Vite, **wouter** (NOT React Router), **TanStack Query v5**, axios, `react-error-boundary`.
- No UI library or CSS framework. **Don't add dependencies (Tailwind, MUI, shadcn, zustand, date-fns...) without asking the user** - it's a team decision and goes into `work/decisions.md`.
- Package manager is **yarn** (`yarn.lock`). Don't use npm/pnpm.

## Structure (existing, respect it)
```
src/
├── api/          # base.ts (axios instance) + <domain>.api.ts
├── components/   # shared components
├── constants/
├── hooks/        # useFundingCalls.ts ... (TanStack Query hooks)
├── layouts/
├── pages/        # one folder per page: HomePage/HomePage.tsx + index.ts
├── routes/       # routes.ts (path constants)
├── types/        # <Name>.type.ts
├── App.tsx       # ONLY providers (ErrorBoundary, QueryClientProvider)
├── Router.tsx    # ONLY <Switch>/<Route>
└── main.tsx
```
- **Every folder has a barrel `index.ts`.** When you add a file, add `export * from "./File.tsx";` to the barrel. New folder = created with `index.ts` right away.
- **Always import through the `@/` alias and the barrel**: `import {HomePageTitle} from "@/components";`, not `../../components/HomePageTitle`.
- A component used by only one page lives in that page's folder (`pages/FundingCallsPage/FundingCallCard.tsx`). It moves to `components/` only once at least two pages use it.
- **All code is in English** (components, hooks, types, files, variables), using the domain glossary in the `spring-boot-api` skill (natječaj → `FundingCall`, propis → `Regulation`...). Only user-visible UI text is Croatian. Never copy Croatian names from `poc/`.

## Components
- **Named export, arrow function, PascalCase**, file name = component name:
  ```tsx
  interface FundingCallCardProps {
      fundingCall: FundingCall;
      onSelect: (id: number) => void;
  }

  export const FundingCallCard = ({fundingCall, onSelect}: FundingCallCardProps) => {
      ...
  };
  ```
  Default export only for `App` (already exists).
- Props type as `interface <Name>Props` right above the component. No `React.FC`.
- **The page (`pages/`) wires up data, components display it.** The page calls a hook (`useFundingCalls`) and passes data down; presentational components don't call the API.
- Component over ~120 lines or with more than one responsibility → split it.
- No `useEffect` for data fetching (TanStack Query does that) and no `useEffect` for derived state (compute it during render). `useEffect` is for syncing with the outside world.
- Lists always with a stable `key` (database id), never the index.

## Writing functions
- One function = one thing; short; **early `return`** instead of nested ternaries.
- Names: handlers `handleX` inside the component, the props for them `onX` (`onSelect` → `handleSelect`). Booleans `isX/hasX`. No abbreviations.
- Pure helpers (formatting, filtering) outside the component or in `utils/` (create the folder with a barrel when needed), so they aren't recreated every render and can be tested.
- **No `any`.** If the type is unknown: `unknown` + narrowing. No `as` casts except at boundaries (e.g. the existing `document.getElementById('root')!`).
- Don't return different types from the same function. For "may be absent" use `undefined`, don't mix with `null`.

## Types
- In `types/<Name>.type.ts`, one domain type per file, exported through `types/index.ts`.
- The type mirrors the backend JSON **exactly** (camelCase, dates as ISO `string`, amounts as `number | null`). If the backend changes the DTO, change the type too.
- For state unions use string literal types (`type FundingCallStatus = "OPEN" | "CLOSED" | "ANNOUNCED";`, values identical to the backend enum), not `enum`.

## API layer
- The only axios instance is `api` from `@/api`. **The interceptor already returns `response.data`** and turns errors into an `Error` with the backend message (`{statusCode, message, error}`). So functions return data directly:
  ```ts
  // api/funding-calls.api.ts
  import {api} from "./base.ts";
  import type {FundingCall, Page} from "@/types";

  export const getFundingCalls = (params: FundingCallFilter) =>
      api.get<Page<FundingCall>, Page<FundingCall>>("/funding-calls", {params});

  export const getFundingCall = (id: number) =>
      api.get<FundingCall, FundingCall>(`/funding-calls/${id}`);
  ```
  (The second generic parameter is needed because the interceptor changes the return type.)
- Paths without the `/api` prefix (already in `baseURL`). Parameters via `params`, not by manually concatenating query strings.
- Dev proxy: if `vite.config.ts` has no `server.proxy` for `/api` → `http://localhost:8080` yet, add it before the first real call (otherwise calls hit Vite and return 404).

## Data fetching (TanStack Query)
- Every fetch goes through a hook in `hooks/`: `useFundingCalls(filter)`, `useFundingCall(id)`. Components don't call `useQuery` with an inline `queryFn`.
- Query keys as arrays from general to specific, defined in one place per domain:
  ```ts
  export const fundingCallKeys = {
      all: ["funding-calls"] as const,
      list: (filter: FundingCallFilter) => [...fundingCallKeys.all, "list", filter] as const,
      detail: (id: number) => [...fundingCallKeys.all, "detail", id] as const,
  };
  ```
- Mutations (`useMutation`) call `queryClient.invalidateQueries({queryKey: fundingCallKeys.all})` on success.
- For filters/search use `placeholderData: keepPreviousData` so the list doesn't flicker.

## Routes (wouter)
- Paths as constants in `routes/routes.ts`, never a string literal in `<Link>`/`navigate`.
- New page = folder in `pages/` + barrel + constant in `routes` + `<Route>` in `Router.tsx` (before the catch-all `NotFoundPage`).
- Params: `useParams()` / `useRoute()` from wouter. Filter state the user may want to share or come back to lives in the URL (`useSearchParams` from wouter), not only in `useState`.

## UI/UX rules (this is scored)
- **Every data screen has 4 states**: loading (skeleton or clear message), error (message from `error.message` + "Pokušaj ponovno" button → `refetch`), empty ("Nema otvorenih natječaja za odabrane filtere" + how to change that), data.
- **All UI text is in Croatian**, short and concrete. No "Lorem ipsum" and no English leftovers in the UI.
- Formatting via `Intl` with `"hr-HR"`: dates (`23. 10. 2026.`), amounts (`new Intl.NumberFormat("hr-HR", {style: "currency", currency: "EUR"})`). Highlight deadlines that are close ("još 3 dana").
- Every piece of data coming from open data has a **visible link to its source** and a source label; fields extracted by AI are marked (as in `poc/web/`). This builds trust and points for "Use of open data".
- Accessibility: semantic HTML (`button` for actions, `a`/`Link` for navigation, `label` for every form field), visible focus, contrast, `alt` on images. Accessibility is explicitly part of challenge A and the jury values it everywhere.
- Responsive: must look good at 1280 px (video) and be usable on mobile.
- Styles: until the team decides otherwise, CSS Modules (`Component.module.css` next to the component) and CSS variables for colours/spacing in one global file. No inline `style={{...}}` except for dynamic values.

## Before saying it's done
1. In `apps/web`: `yarn lint` and `yarn build` pass (rule from CLAUDE.md, mandatory before submission).
2. Check any UI-visible change in the browser (`yarn dev`): happy path + empty state + error (e.g. API stopped).
3. If you added a type for a new endpoint, verify it matches the real JSON from the backend, not an assumed one.
