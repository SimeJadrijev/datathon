# OtvoreniRadar design system

Single source of truth for the look and the frontend conventions of `apps/web`.
Everything here exists in code. Token values live in `apps/web/src/global.css`, components in `apps/web/src/components/` (one folder per component, see section 7).
If you change a token or a component, update this file in the same change.

Code rules (structure, hooks, API, types) are in the `react-web` skill (`.claude/skills/react-web/SKILL.md`). This file covers how the UI looks and behaves.

---

## 1. Philosophy

OtvoreniRadar is a **European civic-tech product**: the usability of a professional SaaS tool with the trust of a public information service.

1. **Trust before decoration.** Every fact shows where it came from. Official data and machine-generated data never look the same.
2. **Information density.** Users compare many funding calls. Compact rows, short labels, no filler whitespace.
3. **Calm.** One brand colour (forest green), warm neutrals, borders instead of shadows, almost no motion.
4. **Honest states.** Every data screen has loading, error, empty and data states. Unknown values say "provjeri na izvoru", never a guess.
5. **Consistency over creativity.** Reuse a component before writing a new one. A new pattern is a design-system change.

## 2. References (concepts, not copies)

| Reference | What we take |
|---|---|
| [Linear](https://linear.app/) | Tight type scale, 1 px borders, small radii, compact rows, active navigation as a thin bar, restrained colour |
| [Welcome to the Jungle](https://www.welcometothejungle.com/en/jobs) | Opportunity discovery: scannable cards with key facts on the right, filters as chips, "why this fits you" |
| [GOV.UK Find a grant](https://find-government-grants.service.gov.uk/) | Plain language, visible source, eligibility and deadlines up front, accessibility as a baseline |

## 3. Colour tokens

Defined on `:root` in `global.css`. Never write a hex value in a component; use the token.

### Neutrals
| Token | Hex | Use |
|---|---|---|
| `--color-background` | `#F7F7F3` | Page canvas (warm off-white) |
| `--color-surface` | `#FFFFFF` | Cards, header, footer, inputs |
| `--color-surface-subtle` | `#F2F3EE` | Inset panels (OIB lookup), neutral badges, icon tiles |
| `--color-surface-hover` | `#EEF0EA` | Hover background of rows, ghost buttons, menu items |
| `--color-border` | `#DFE4DC` | Default 1 px border, dividers |
| `--color-border-strong` | `#C3CBC0` | Hover border, radio outline |
| `--color-skeleton` | `#E9ECE5` | Skeleton bars |
| `--color-text` | `#1D2420` | Body text and headings (near-neutral ink: green is the accent, not a tint on every pixel) |
| `--color-text-muted` | `#59625D` | Secondary text, labels, hints (5.9:1 on canvas) |
| `--color-text-subtle` | `#7A877F` | Placeholders and decorative icons only (3.5:1, never for essential text) |
| `--color-text-inverse` | `#FFFFFF` | Text on primary |

### Brand
| Token | Hex | Use |
|---|---|---|
| `--color-primary` | `#244A3D` | Primary buttons, links, active navigation bar, focus outline, selected state border |
| `--color-primary-hover` | `#1B392F` | Hover of primary |
| `--color-primary-soft` | `#E7EEE9` | Selected option / chip background, profile badges |
| `--color-primary-border` | `#B7C9BD` | Border of `primary` badge |

### Semantic
| Token pair | Hex (text / background) | Use |
|---|---|---|
| `--color-success` / `-soft` | `#2B6A3C` / `#E5F0E6` | Status "Otvoren", confirmed data, match reasons |
| `--color-warning` / `-soft` | `#85570A` / `#F8EFDA` | Deadline within 7 days, unconfirmed status |
| `--color-danger` / `-soft` | `#A3321F` / `#F8E7E2` | Errors, expired deadline, destructive actions |
| `--color-info` / `-soft` | `#3B5674` / `#E8EDF3` | Status "Najavljen", neutral information notices |
| `--color-ai` / `-soft` | `#6B4766` / `#F4EDF3` | Machine-generated content. Its own hue (never the same as `info`, which is the "Najavljen" status). Marks the **value itself** with a dashed underline or dashed frame, plus one sentence saying to check the source. No sparkles icon |

All text/background pairs above meet WCAG AA (4.5:1) for normal text.

## 4. Typography

- **Fonts** (Google Fonts, loaded in `index.html`, both cover č ć đ š ž):
  - `--font-family-display`: **Source Serif 4** 600 for `h1` and `h2` (set globally) and the day number in `DeadlineDate`. Editorial, like an official gazette. Why: Inter-only UIs are the most common "AI-generated" look.
  - `--font-family`: **IBM Plex Sans** 400/500/600 for everything else, fallback `system-ui`.
- **Weights:** `--font-weight-regular` 400, `--font-weight-medium` 500, `--font-weight-semibold` 600. No 700+.
- **Headings** (`h1`-`h4`) are semibold, `--line-height-tight`, `--letter-spacing-tight` (-0.01em), `text-wrap: balance` (set globally). `h3` and smaller stay sans.
- **Numbers** in amounts and dates use the `.tabular-nums` utility class.
- **Minimum sizes:** body 16 px, nothing under 13 px. The demo video is recorded at 1280 px and compressed; smaller text is unreadable there.

| Token | Size | Use |
|---|---|---|
| `--font-size-xs` | 13 px | Captions, source line, fact labels |
| `--font-size-sm` | 14 px | Secondary text, buttons, labels, nav, hints |
| `--font-size-md` | 16 px | Body (default on `body`) |
| `--font-size-lg` | 18 px | Row titles, form step legends, state-panel titles |
| `--font-size-xl` | 24 px | Section titles (`h2` in cards), page title on mobile |
| `--font-size-2xl` | 34 px | Page title (`h1` in `PageHeader`) |

Line heights: `--line-height-tight` 1.25 (headings), `--line-height-snug` 1.4 (dense UI text), `--line-height` 1.55 (body).
No uppercase "eyebrow" labels above titles.

## 5. Spacing, layout, breakpoints

Spacing tokens (4 px grid): `--space-1` 4, `--space-2` 8, `--space-3` 12, `--space-4` 16, `--space-5` 24, `--space-6` 32, `--space-7` 48, `--space-8` 64.

| Token | Value | Use |
|---|---|---|
| `--content-width` | 1200 px | Max width of header, main, footer |
| `--content-width-narrow` | 760 px | Reading pages (regulation text, long details) |
| `--aside-width` | 320 px | Right sidebar in two-column pages |
| `--header-height` | 56 px | Sticky header |
| `--gutter` | 24 px (16 px under 768 px) | Side padding of the page |

Breakpoints (CSS cannot use variables in media queries, so use these literal values):
- `max-width: 960px`: two-column layouts collapse to one column, sidebar stops being sticky.
- `max-width: 768px`: mobile. Navigation becomes a menu, cards stack their facts, controls grow to 42-44 px.
- `max-width: 480px` / `400px`: very small phones, rows of controls stack.

Typical vertical rhythm: `PageHeader` → `--space-5` → content; gap between list items `--space-3`; gap between form sections `--space-6`.

## 6. Shape, depth, icons

- Radii: `--radius-xs` 4 px (badges, step numbers), `--radius-sm` 6 px (buttons, inputs, chips, notices), `--radius-md` 8 px (cards, panels). `--radius-pill` only for spinners/dots. **Nothing larger than 8 px.**
- Borders: 1 px `--color-border` on every surface. Hover raises it to `--color-border-strong`.
- Shadows: `--shadow-sm` only on controls (buttons, inputs, selects) to make them feel pressable. `--shadow-popover` only for things that float (mobile menu, select picker). Cards have **no** shadow.
- Focus: `:focus-visible` gives a 2 px `--color-primary` outline (global). Inputs use border + `--focus-ring` instead.
- Motion: only colour/border/background transitions, `--duration-fast` (120 ms) with `--easing`. Reduced motion is respected globally.
- **Icons:** `Icon` component (`components/Icon/Icon.tsx`) with Lucide paths, stroke 1.75, 16 px default. Icons are decorative (`aria-hidden`), the text carries meaning. Need a new icon? Copy its path from lucide.dev into `ICON_PATHS`. Do not add an icon library.
- **Use icons sparingly:** only where they carry function (external link, menu, close, retry, alert, check on a selected chip, empty/error state). No icon in front of every label (provider, source, list links) and no icons in bordered square tiles.

Available icons: `alert`, `arrowRight`, `calendar`, `check`, `close`, `database`, `document`, `euro`, `externalLink`, `info`, `institution`, `mapPin`, `menu`, `retry`, `search`.

## 7. Components (`@/components`)

Folder structure: every component has its own folder with the component, its CSS Module and a barrel.
```
components/
├── Badge/
│   ├── Badge.tsx
│   ├── Badge.module.css
│   └── index.ts          # export * from "./Badge.tsx";
├── StatePanel/           # EmptyState, ErrorState, LoadingState share StatePanel.module.css
└── index.ts              # export * from "./Badge"; ... one line per folder
```
- Pages import only from the main barrel: `import {Badge} from "@/components";`.
- Components import each other through the folder barrel, `import {Icon} from "@/components/Icon";`, never through `@/components` (that would be a circular import).
- New component = new folder + `index.ts` + one line in `components/index.ts`. Components that share one stylesheet share one folder.

| Component | Props | Use |
|---|---|---|
| `Button` | `variant`: `primary` \| `secondary` \| `ghost` \| `danger`; `size`: `md` (38 px) \| `sm` (30 px); all `<button>` props | Actions. One `primary` per view section. `danger` is text-only for destructive actions ("Obriši profil") |
| `ButtonLink` | `href`, `variant`, `size`, `className` | Navigation that looks like a button (wouter `Link`) |
| `TextInput` | all `<input>` props + `ref` | Every text field. Set `aria-invalid` for errors, `aria-describedby` for hint/error ids |
| `Select` | all `<select>` props | Every dropdown (customisable select in Chromium, native elsewhere) |
| `Badge` | `tone`: `neutral` \| `primary` \| `success` \| `warning` \| `danger` \| `info` \| `ai`; `icon`; `title` | Short status/category labels. Max ~3 per row |
| `Notice` | `tone`: `info` \| `success` \| `warning` \| `danger`; `title` | Inline message inside a card or form |
| `Card` | `as`: `div` \| `section` \| `article`; `className`; `aria-labelledby` | Plain bordered surface for forms and panels |
| `FundingCallList` | `children` (`<li>` elements) | Framed list with dividers. Every list of funding calls |
| `FundingCallRow` | `fundingCall`, `children` (extra lines such as match reasons) | One funding call inside `FundingCallList` |
| `DeadlineDate` | `deadline`, `isExtractedByAi`, `isCompact` | Calendar-style date block (month / day). Urgent = warning frame, AI = dashed frame. `isCompact`: no frame, day over month, urgent day in warning colour, AI day underlined dashed. Decorative, pair it with the deadline in words |
| `PageHeader` | `title`, `description`, `meta`, `actions` | Top of every page, exactly one `h1`. `meta` is a line of real facts (counts, last update), never a slogan |
| `EmptyState` | `title`, `hint`, `icon`, `actions` | No data / not built yet / 404 |
| `ErrorState` | `title`, `message`, `onRetry` | Failed request. Always pass `onRetry={() => refetch()}` |
| `ListSkeleton` | `label`, `rows` | Loading state of lists |
| `LoadingState` | `message` | Loading state of non-list content (spinner) |
| `ExternalLink` | `href`, `className` | Any link leaving the app. Adds icon + "(otvara se u novoj kartici)" for screen readers |
| `Icon` | `name`, `size`, `className` | See section 6 |

Patterns that live in a page folder (used by one page), reuse them by moving to their own `components/<Name>/` folder when a second page needs them:
- **Choice tiles** (`ProfileForm.module.css` `.option`): radio cards with a drawn radio dot, selected = primary border + primary-soft background.
- **Filter chips** (`.chip`): 6 px radius toggle buttons, selected = primary text/border + primary-soft + check icon. Use the same look for list filters.
- **Match reasons** (`RecommendationsPage/MatchDetails`): one line of plain text, "**Odgovara ti:** reason · reason · reason". No coloured box, no check icons.
- **Side panel** (`HomePage/SidePanel.module.css`), two variants on the home page:
  - **Kako radi** (`HowItWorks`, no profile yet): three steps with serif numbers "1." in primary green (no boxes), then a divider and "Ili pregledaj bez profila" as plain text links with a muted description (no icon tiles).
  - **Sljedeći rokovi za tebe** (`ProfileDeadlines`, profile exists): the 3 nearest future deadlines among the profile's own matches (same query as "Moji natječaji"), compact `DeadlineDate` (frameless "30 / lis") + full title + "još N dana · provider". Deadlines within 7 days get a "Rok uskoro" label in `--color-warning` above the title.
- **Fast path + "ili"** (`HomePage/OibLookup` + `ProfileForm` `.or`): the OIB lookup is an open primary-soft panel at the top of the form, then a divider line with a green "ili" circle, then the manual steps. The circle is the one deliberate use of `--radius-pill` outside spinners and dots.
- **Titles are never truncated** (no `line-clamp`, no ellipsis) in lists. Official titles carry the details people look for.

### Funding call row anatomy
```
┌─────┬──────────────────────────────────────────────────┬──────────────┐
│ stu │ Bespovratna sredstva · Provider (muted, 1 line)   │   do 7.500 € │
│  2  │ Title, semibold 18 px, full length                │    po prijavi │
│     │ Odgovara ti: područje · skupina   (children)      │   još 4 dana │
│     │ Izvor: FZOEU · AI note if extracted               │ Službena ↗   │
├─────┴──────────────────────────────────────────────────┴──────────────┤
│ next row (1 px divider, no gap, no separate card)                     │
```
The status badge shows only when the call is **not** open ("Najavljen", "Zatvoren"); "Otvoren" is the default and does not need a label. The whole row is clickable (title link stretched over it); the external link sits above that layer. On mobile the right column moves under the title as one line.

## 8. Page layouts

- **Shell** (`layouts/AppLayout`): sticky white header (brand left, nav right, active item = 2 px green bar on the bottom border), `main` max 1200 px, white footer with source link and disclaimer. Skip link to `#main-content`.
- **Discovery / home:** `PageHeader` with a facts line (`meta`: open calls, regulations, last update from `GET /api/meta`) → two columns (profile form + 320 px sticky aside: "Kako radi" without a profile, "Sljedeći rokovi za tebe" with one). No hero banners, no illustrations, no boxed step numbers.
- **Lists** (funding calls, regulations): `PageHeader` (title + result description + actions) → filter row (chips/selects) → result summary as one plain sentence ("**N natječaja** za profil **Tvrtka** (energetika, cijela Hrvatska)") → `FundingCallList` of `FundingCallRow`. Filters in the URL.
- **Details:** narrow reading column (`--content-width-narrow`) or main + aside. Deadline, provider and "Službena objava" link visible without scrolling. AI fields get a dashed underline in `--color-ai` and a sentence that says to check the source.
- **Regulations:** editorial look: title, then publication date and effective date as **two separate facts**, then the link to the official text (Narodne novine) before any summary.
- **Settings-like screens** (profile summary): `dl` with label column + value column, actions in a bordered footer row, destructive action right-aligned.

## 9. Interaction states

| State | Rule |
|---|---|
| Hover | Border to `--color-border-strong` or background to `--color-surface-hover`. Primary darkens. No lift, no scale |
| Active / selected | `--color-primary` border or bar + `--color-primary-soft` background. Never only colour: add a check icon or radio dot |
| Focus | Visible 2 px green outline (`:focus-visible`), inputs use `--focus-ring`. Never remove focus without a replacement |
| Disabled | 55 % opacity, `not-allowed` cursor; busy buttons change their label ("Dohvaćam…") |
| Destructive | No confirmation dialog: act at once and offer undo ("Profil je obrisan." + "Vrati profil") in an `aria-live` region |
| Loading | `ListSkeleton` for lists, `LoadingState` otherwise. Keep the page header visible |
| Empty | `EmptyState` with a concrete hint how to get results and an action |
| Error | `ErrorState` with the API `message` and "Pokušaj ponovno"; form errors inline under the field in `--color-danger` |

## 10. Accessibility and responsiveness

- WCAG 2.1 AA: contrast per section 3, one `h1` per page, landmarks (`header`, `nav`, `main`, `aside`, `footer`).
- Every input has a `<label>`; groups use `fieldset` + `legend`. Hints and errors are linked with `aria-describedby`, invalid fields get `aria-invalid`.
- Status changes are announced (`aria-live="polite"` on loading and async results, `role="alert"` on errors).
- Keyboard: everything reachable by Tab, visually hidden native inputs stay focusable (`.visually-hidden`), focus moves to the first invalid field on submit.
- Headings never skip a level (a list of `h3` rows sits under an `h2`, even if that `h2` is styled as a plain summary line).
- Inputs have `name`; codes (OIB) get `spellCheck={false}`; non-auth fields `autoComplete="off"`. Placeholders show an example ("npr. …").
- Touch targets ≥ 42 px under 768 px. Inputs use 16 px text on mobile (no iOS zoom). Controls have `touch-action: manipulation` (global).
- Brand name and other identifiers get `translate="no"`.
- Check at 1280 px (video) and 390 px (phone). No horizontal scroll.
- Language: `<html lang="hr">`.

## 11. Croatian UI copy

- All visible text is Croatian; code, comments and logs are English.
- Address the user with **"ti"** (informal), short sentences, verbs first on buttons: "Prikaži moje natječaje", "Dohvati podatke", "Pokušaj ponovno".
- Sentence case everywhere ("Moji natječaji", not "Moji Natječaji"). This deliberately overrides the Title Case rule in `web-design-guidelines`, which is written for English.
- Typography: `…` not `...` (loading text ends with it: "Učitavanje…"), Croatian quotes „…”.
- Dates `03. 11. 2026.` and amounts `7.500 €` via `Intl` with `hr-HR` (`utils/date.ts`, `utils/formatAmount.ts`). Relative deadlines: "još 5 dana", "ističe sutra", "isteklo".
- Glossary: natječaj (funding call), javni poziv, bespovratna sredstva, potpora, stipendija, propis, zakon, izvor, službena objava, rok, iznos, prihvatljivost (eligibility).
- Never invent facts. Missing value → "provjeri na izvoru". AI content → "AI podaci" / "AI sažetak" + "provjeri na izvoru".
- Croatian quotes for user input: „solarni".

## 12. Do and don't

| Do | Don't |
|---|---|
| `<Badge tone="info">Najavljen</Badge>` only when it adds information | Inline `style={{color: "green"}}` or a new hex value; a badge row on every item |
| `border: 1px solid var(--color-border)` on a card | `box-shadow: 0 10px 30px ...` on a card |
| `border-radius: var(--radius-md)` | `border-radius: 16px` / 24 px "bubbly" cards |
| Facts in a compact `dl` (Rok, Iznos) | Big KPI tiles for single numbers |
| `<ExternalLink href={url}>Službena objava</ExternalLink>` | A bare URL or an external link without the icon |
| Dashed underline/frame in `--color-ai` on the AI-read value + "provjeri na izvoru" | Showing an AI-extracted deadline exactly like an official one; a sparkles icon |
| One framed `FundingCallList` with dividers | A stack of separate cards with gaps, each with its own coloured inner box |
| A line of real numbers under the title (`PageHeader meta`) | Slogans, eyebrows, step numbers in coloured boxes |
| `ListSkeleton` while a list loads | A full-screen spinner that hides the page header |
| `Icon name="check"` from `@/components` | Pasting a new inline SVG into a page |

## 13. Anti-patterns (never)

Purple/blue gradients, glowing or blurred backgrounds, decorative orbs, glassmorphism, radii above 8 px, heavy shadows, oversized hero sections, decorative illustrations, KPI cards as decoration, emoji in UI, more than one primary button per section, animations beyond colour transitions, English leftovers in the UI, fake or "example" data without a clear `[TEST]`/mock label.

The "template look" we avoid on purpose (typical of AI-generated UIs): Inter everywhere, uppercase eyebrow above every title, icons in bordered square tiles, an icon before every label, sparkles for AI, step numbers in coloured boxes, a coloured box inside every card, and every item wrapped in its own card.

## 14. Checklist for a new page or component

1. Read this file and check `@/components` for something to reuse.
2. Use tokens only: no hex, no raw px for spacing/font sizes (1 px borders and icon sizes are fine).
3. One `PageHeader` (one `h1`), layout from section 8.
4. Four states: loading (`ListSkeleton`/`LoadingState`), error (`ErrorState` + retry), empty (`EmptyState` + hint + action), data.
5. Every open-data fact has a source label and an `ExternalLink`; AI fields use the dashed `--color-ai` marking.
6. Keyboard, focus, labels, `aria-*` per section 10; check 1280 px and 390 px.
7. Run the `web-design-guidelines` skill on the changed files (UX and accessibility rules, see `.claude/skills/web-design-guidelines/`).
8. Croatian copy per section 11.
9. `yarn lint` and `yarn build` pass in `apps/web`.
10. New token, component or pattern? Add it to this file in the same change. Deliberate deviation? Write it down here with the reason.
