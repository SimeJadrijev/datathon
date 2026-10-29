# OtvoreniRadar design system

Single source of truth for the look and the frontend conventions of `apps/web`.
Everything here exists in code. Token values live in `apps/web/src/global.css`, components in `apps/web/src/components/`.
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
| `--color-text` | `#203B33` | Body text and headings |
| `--color-text-muted` | `#56675F` | Secondary text, labels, hints (5.2:1 on canvas) |
| `--color-text-subtle` | `#7A877F` | Placeholders and decorative icons only (3.5:1, never for essential text) |
| `--color-text-inverse` | `#FFFFFF` | Text on primary |

### Brand
| Token | Hex | Use |
|---|---|---|
| `--color-primary` | `#244A3D` | Primary buttons, links, active navigation bar, focus outline, selected state border |
| `--color-primary-hover` | `#1B392F` | Hover of primary |
| `--color-primary-soft` | `#E7EEE9` | Selected option / chip background, profile badges |
| `--color-primary-border` | `#B7C9BD` | Border of `primary` badge, step numbers |
| `--color-sage` | `#8AA596` | Reserved accent (charts, illustrations of data) |
| `--color-sage-soft` | `#EEF2EC` | "Zašto ti odgovara" panel |

### Semantic
| Token pair | Hex (text / background) | Use |
|---|---|---|
| `--color-success` / `-soft` | `#2B6A3C` / `#E5F0E6` | Status "Otvoren", confirmed data, match reasons |
| `--color-warning` / `-soft` | `#85570A` / `#F8EFDA` | Deadline within 7 days, unconfirmed status |
| `--color-danger` / `-soft` | `#A3321F` / `#F8E7E2` | Errors, expired deadline, destructive actions |
| `--color-info` / `-soft` | `#3B5674` / `#E8EDF3` | Status "Najavljen", neutral information notices |
| `--color-ai` / `-soft` | `#3B5674` / `#E8EDF3` | Machine-generated content. Always with a **dashed** border and the sparkles icon |

All text/background pairs above meet WCAG AA (4.5:1) for normal text.

## 4. Typography

- **Font:** Inter (Google Fonts, weights 400/500/600, loaded in `index.html`), fallback `system-ui`. Croatian diacritics (č ć đ š ž) are covered by the latin-ext subset.
- **Weights:** `--font-weight-regular` 400, `--font-weight-medium` 500, `--font-weight-semibold` 600. No 700+.
- **Headings** (`h1`-`h4`) are semibold, `--line-height-tight`, `--letter-spacing-tight` (-0.015em), `text-wrap: balance` (set globally).
- **Numbers** in amounts and dates use the `.tabular-nums` utility class.

| Token | Size | Use |
|---|---|---|
| `--font-size-xs` | 12 px | Badges, captions, fact labels, eyebrow |
| `--font-size-sm` | 13 px | Secondary text, buttons, labels, nav, hints |
| `--font-size-md` | 15 px | Body (default on `body`) |
| `--font-size-lg` | 17 px | Card titles, form step legends, state-panel titles |
| `--font-size-xl` | 22 px | Section titles (`h2` in cards), page title on mobile |
| `--font-size-2xl` | 30 px | Page title (`h1` in `PageHeader`) |

Line heights: `--line-height-tight` 1.25 (headings), `--line-height-snug` 1.4 (dense UI text), `--line-height` 1.55 (body).
Eyebrow labels: xs, semibold, uppercase, `--letter-spacing-caps`, muted colour.

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
- **Icons:** `Icon` component (`components/Icon.tsx`) with Lucide paths, stroke 1.75, 16 px default. Icons are decorative (`aria-hidden`), the text carries meaning. Need a new icon? Copy its path from lucide.dev into `ICON_PATHS`. Do not add an icon library.

Available icons: `alert`, `arrowRight`, `calendar`, `check`, `close`, `database`, `document`, `euro`, `externalLink`, `info`, `institution`, `mapPin`, `menu`, `retry`, `search`, `sparkles`.

## 7. Components (`@/components`)

| Component | Props | Use |
|---|---|---|
| `Button` | `variant`: `primary` \| `secondary` \| `ghost` \| `danger`; `size`: `md` (38 px) \| `sm` (30 px); all `<button>` props | Actions. One `primary` per view section. `danger` is text-only for destructive actions ("Obriši profil") |
| `ButtonLink` | `href`, `variant`, `size`, `className` | Navigation that looks like a button (wouter `Link`) |
| `TextInput` | all `<input>` props + `ref` | Every text field. Set `aria-invalid` for errors, `aria-describedby` for hint/error ids |
| `Select` | all `<select>` props | Every dropdown (customisable select in Chromium, native elsewhere) |
| `Badge` | `tone`: `neutral` \| `primary` \| `success` \| `warning` \| `danger` \| `info` \| `ai`; `icon`; `title` | Short status/category labels. Max ~3 per row |
| `Notice` | `tone`: `info` \| `success` \| `warning` \| `danger`; `title` | Inline message inside a card or form |
| `Card` | `as`: `div` \| `section` \| `article`; `className`; `aria-labelledby` | Plain bordered surface for forms and panels |
| `FundingCallCard` | `fundingCall`, `children` (extra rows such as match reasons) | The funding call list item. Use it in every list of calls |
| `PageHeader` | `title`, `eyebrow`, `description`, `actions` | Top of every page, exactly one `h1` |
| `EmptyState` | `title`, `hint`, `icon`, `actions` | No data / not built yet / 404 |
| `ErrorState` | `title`, `message`, `onRetry` | Failed request. Always pass `onRetry={() => refetch()}` |
| `ListSkeleton` | `label`, `rows` | Loading state of lists |
| `LoadingState` | `message` | Loading state of non-list content (spinner) |
| `ExternalLink` | `href`, `className` | Any link leaving the app. Adds icon + "(otvara se u novoj kartici)" for screen readers |
| `Icon` | `name`, `size`, `className` | See section 6 |

Patterns that live in a page folder (used by one page), reuse them by moving to `components/` when a second page needs them:
- **Choice tiles** (`ProfileForm.module.css` `.option`): radio cards with a drawn radio dot, selected = primary border + primary-soft background.
- **Filter chips** (`.chip`): 6 px radius toggle buttons, selected = primary text/border + primary-soft + check icon. Use the same look for list filters.
- **Link list** (`HomePage/BrowseLinks`): icon tile + label + description + arrow, full-row hover.
- **Match reasons** (`RecommendationsPage/MatchDetails`): sage-soft strip with check icons.

### Funding call card anatomy
```
┌──────────────────────────────────────────────┬──────────────────┐
│ [Otvoren] [Bespovratna sredstva] [✧ AI podaci]│ Rok              │
│ Title, semibold 17 px, max 2 lines            │ 03. 11. 2026. [još 5 dana] │
│ 🏛 Provider                                   │ Iznos  do 7.500 € │
├──────────────────────────────────────────────┴──────────────────┤
│ extra rows (children): match reasons, notices                    │
├──────────────────────────────────────────────────────────────────┤
│ ⛁ Izvor: <source name>                      Službena objava ↗    │
└──────────────────────────────────────────────────────────────────┘
```
The whole card is clickable (title link stretched over it); the source link sits above that layer. On mobile the facts move under the title.

## 8. Page layouts

- **Shell** (`layouts/AppLayout`): sticky white header (brand left, nav right, active item = 2 px green bar on the bottom border), `main` max 1200 px, white footer with source link and disclaimer. Skip link to `#main-content`.
- **Discovery / home:** `PageHeader` with eyebrow → two columns (main card + 320 px sticky aside). No hero banners, no illustrations.
- **Lists** (funding calls, regulations): `PageHeader` (title + result description + actions) → filter row (chips/selects) → result summary line ("Pronašli smo **N natječaja**", active filters as badges) → `ol` of cards with `--space-3` gap. Filters in the URL.
- **Details:** narrow reading column (`--content-width-narrow`) or main + aside. Deadline, provider and "Službena objava" link visible without scrolling. AI fields marked with the `ai` badge and a sentence that says to check the source.
- **Regulations:** editorial look: title, then publication date and effective date as **two separate facts**, then the link to the official text (Narodne novine) before any summary.
- **Settings-like screens** (profile summary): `dl` with label column + value column, actions in a bordered footer row, destructive action right-aligned.

## 9. Interaction states

| State | Rule |
|---|---|
| Hover | Border to `--color-border-strong` or background to `--color-surface-hover`. Primary darkens. No lift, no scale |
| Active / selected | `--color-primary` border or bar + `--color-primary-soft` background. Never only colour: add a check icon or radio dot |
| Focus | Visible 2 px green outline (`:focus-visible`), inputs use `--focus-ring`. Never remove focus without a replacement |
| Disabled | 55 % opacity, `not-allowed` cursor; busy buttons change their label ("Dohvaćam...") |
| Loading | `ListSkeleton` for lists, `LoadingState` otherwise. Keep the page header visible |
| Empty | `EmptyState` with a concrete hint how to get results and an action |
| Error | `ErrorState` with the API `message` and "Pokušaj ponovno"; form errors inline under the field in `--color-danger` |

## 10. Accessibility and responsiveness

- WCAG 2.1 AA: contrast per section 3, one `h1` per page, landmarks (`header`, `nav`, `main`, `aside`, `footer`).
- Every input has a `<label>`; groups use `fieldset` + `legend`. Hints and errors are linked with `aria-describedby`, invalid fields get `aria-invalid`.
- Status changes are announced (`aria-live="polite"` on loading and async results, `role="alert"` on errors).
- Keyboard: everything reachable by Tab, visually hidden native inputs stay focusable (`.visually-hidden`), focus moves to the first invalid field on submit.
- Touch targets ≥ 42 px under 768 px. Inputs use 16 px text on mobile (no iOS zoom).
- Check at 1280 px (video) and 390 px (phone). No horizontal scroll.
- Language: `<html lang="hr">`.

## 11. Croatian UI copy

- All visible text is Croatian; code, comments and logs are English.
- Address the user with **"ti"** (informal), short sentences, verbs first on buttons: "Prikaži moje natječaje", "Dohvati podatke", "Pokušaj ponovno".
- Sentence case everywhere ("Moji natječaji", not "Moji Natječaji"). Eyebrows are the only uppercase text (via CSS).
- Dates `03. 11. 2026.` and amounts `7.500 €` via `Intl` with `hr-HR` (`utils/date.ts`, `utils/formatAmount.ts`). Relative deadlines: "još 5 dana", "ističe sutra", "isteklo".
- Glossary: natječaj (funding call), javni poziv, bespovratna sredstva, potpora, stipendija, propis, zakon, izvor, službena objava, rok, iznos, prihvatljivost (eligibility).
- Never invent facts. Missing value → "provjeri na izvoru". AI content → "AI podaci" / "AI sažetak" + "provjeri na izvoru".
- Croatian quotes for user input: „solarni".

## 12. Do and don't

| Do | Don't |
|---|---|
| `<Badge tone="success">Otvoren</Badge>` | Inline `style={{color: "green"}}` or a new hex value |
| `border: 1px solid var(--color-border)` on a card | `box-shadow: 0 10px 30px ...` on a card |
| `border-radius: var(--radius-md)` | `border-radius: 16px` / 24 px "bubbly" cards |
| Facts in a compact `dl` (Rok, Iznos) | Big KPI tiles for single numbers |
| `<ExternalLink href={url}>Službena objava</ExternalLink>` | A bare URL or an external link without the icon |
| `ai` badge with sparkles + dashed border for LLM fields | Showing an AI-extracted deadline exactly like an official one |
| `ListSkeleton` while a list loads | A full-screen spinner that hides the page header |
| `Icon name="check"` from `@/components` | Pasting a new inline SVG into a page |

## 13. Anti-patterns (never)

Purple/blue gradients, glowing or blurred backgrounds, decorative orbs, glassmorphism, radii above 8 px, heavy shadows, oversized hero sections, decorative illustrations, KPI cards as decoration, emoji in UI, more than one primary button per section, animations beyond colour transitions, English leftovers in the UI, fake or "example" data without a clear `[TEST]`/mock label.

## 14. Checklist for a new page or component

1. Read this file and check `@/components` for something to reuse.
2. Use tokens only: no hex, no raw px for spacing/font sizes (1 px borders and icon sizes are fine).
3. One `PageHeader` (one `h1`), layout from section 8.
4. Four states: loading (`ListSkeleton`/`LoadingState`), error (`ErrorState` + retry), empty (`EmptyState` + hint + action), data.
5. Every open-data fact has a source label and an `ExternalLink`; AI fields use the `ai` badge.
6. Keyboard, focus, labels, `aria-*` per section 10; check 1280 px and 390 px.
7. Croatian copy per section 11.
8. `yarn lint` and `yarn build` pass in `apps/web`.
9. New token, component or pattern? Add it to this file in the same change. Deliberate deviation? Write it down here with the reason.
