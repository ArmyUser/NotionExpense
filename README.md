# Notion Expense

An Android app for recording expenses in seconds and syncing them into an
existing Notion finance tracker.

Notion is a good place to *keep* a budget and a poor place to *enter* one. Adding
a row on your phone means opening the app, finding the database, creating a page
and filling four properties — enough friction that small expenses go unrecorded.
Notion Expense reduces that to a few taps: enter the amount, confirm the details,
done. The write to Notion happens in the background, and works even with no
connection.

## The flow

```
Amount → Description → Date → Account → Category → Done
```

One decision per step, with a numeric keypad for the amount and a progress
indicator so you always know where you are. Pressing Done shows an immediate
local confirmation — the app has accepted the expense — while the actual sync to
Notion continues in the background.

## Features

- **Fast entry** — a stepped flow designed for one-handed use
- **Numeric keypad** with long-press on backspace to clear the amount
- **Haptic feedback** on the keypad, navigation and selection
- **Dark theme** throughout
- **Account and Category pickers** as full screens, not dropdowns, so scrolling
  never dismisses them by accident
- **Accounts and Categories read from Notion**, so the app always offers exactly
  the options that exist in your workspace
- **Offline cache** — both lists remain usable with no connection
- **Offline submission** — an expense entered without network is stored on the
  device and sent when connectivity returns
- **Background sync via WorkManager**, surviving app close, process death and
  reboot
- **Automatic retries** with exponential backoff for transient failures
  (network errors, rate limits, Notion server errors)
- **Honest notifications** — a success notification is sent *only* after Notion
  confirms the page was created; a failure notification is sent only once the
  work has permanently failed or exhausted its retries. Nothing is sent while a
  retry is still pending.
- **Last-used Account and Category are remembered** and preselected for the next
  expense, provided they still exist in Notion

> **Tip:** the app is built to be opened by a gesture rather than by hunting for
> an icon. On Samsung devices, Good Lock → RegiStar → Back-Tap action → Double
> Tap → Open app makes a double tap on the back of the phone launch it, in the
> spirit of the iPhone Back Tap shortcut. This is a device-level setting, not a
> feature of the app.

## Notion integration

Notion is the source of truth. The app **does not create a finance tracker** and
does not modify your schema, views or properties — it reads from and writes to a
structure you already own.

```
Finance Tracker
├── Expenses     ← a new page is created here per expense
├── Accounts     ← read to populate the Account picker
├── Categories   ← read to populate the Category picker
├── Incomes      (untouched)
└── Transfers    (untouched)
```

For each submitted expense the app creates one page in the **Expenses** data
source, setting the title, amount and date, and linking **Account** and
**Category** as Notion **relations** to the corresponding rows. Because the page
IDs are captured when the lists are loaded, the relations stay correct even when
the expense is sent hours later from the offline queue.

Accounts and Categories are read-only to the app: it never creates, renames or
deletes rows in them.

The integration targets Notion's data-source API (`Notion-Version: 2026-03-11`),
so a database's **data source ID** is required, not its database ID — the setup
below shows how to retrieve it.

## Requirements

- Android Studio, JDK 17
- `minSdk 26`, `compileSdk 35`
- An existing Notion database with expense, account and category tables

## Setup

**1. Clone**

```bash
git clone https://github.com/ArmyUser/AndroidExpenseSync.git && cd AndroidExpenseSync
```

**2. Create a Notion integration**

At <https://www.notion.so/my-integrations>, create an internal integration.
Grant it **Insert content** only — the app never needs broader access. Copy the
token.

**3. Connect it to your database**

Open your finance tracker in Notion and use **⋯ → Connections → Connect to →
your integration**. Repeat for the Accounts and Categories tables if they are
separate pages. Skipping this step produces `404 object_not_found`, not a
permission error.

**4. Configure local Gradle properties**

Copy the keys from [`gradle.properties.example`](gradle.properties.example) into
`~/.gradle/gradle.properties` and fill in your own values. That file lives
outside the repository and is never committed.

```properties
notionToken=
notionDataSourceId=
notionCategoryDataSourceId=
notionAccountsDataSourceId=
notionPropTitle=
notionPropAmount=
notionPropDate=
notionPropCategory=
notionPropAccount=
notionPropCategoryType=
```

The example file includes the `curl` commands for retrieving your data source
IDs and for listing your real property names and types. Property names are read
from configuration rather than hard-coded, so the app adapts to your schema
without code changes.

**5. Build and install**

```bash
./gradlew :app:installDebug
```

The project compiles with no configuration at all. In that state the app runs
normally and simply reports that Notion is not configured instead of syncing.

## Security

No token, credential or workspace identifier is stored in this repository.

- Secrets live in `~/.gradle/gradle.properties`, outside the working tree, and
  are injected into `BuildConfig` at build time
- `gradle.properties.example` is a template containing placeholders only
- `local.properties` is machine-specific (it holds your local SDK path) and is
  not committed; Android Studio regenerates it
- Generated output under `app/build/` — which does contain the compiled
  credentials — is ignored by Git

The token is an internal integration token scoped to a single workspace. Treat
the built APK as sensitive: anything compiled into `BuildConfig` can be
extracted from it.

## License

Not currently licensed for redistribution.
