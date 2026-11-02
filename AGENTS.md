# Agent guidelines

Instructions for AI coding agents working in this repository. See `README.md` for what the
project is, its settings, and how to set it up.

## Development

- Set up: JDK 17 or 21 and Docker; use `./gradlew`, which pins Gradle, and `build.gradle` pins
  Spotless and its google-java-format, so local checks match CI. Services: Oracle (ports 1521
  and 5500), Keycloak (8081 and 9991), Puppet Show (3000), MailHog (1025 and 8025), and dtm built
  from the working tree (8080, 8443 and 9990), all by `build.yaml`:
  `docker compose -f build.yaml up -d --build` (about two minutes the first time, with the image
  build and Oracle's setup). The ports and container names (`dtm-oracle`,
  `dtm-dev`, ...) are fixed, so only one stack runs per VM, and other projects use some of the
  same ports: check `ss -ltnp` and `docker ps` first, and if a port is taken, ask the person
  you work with rather than stopping what holds it. `deps.yaml` alone names its containers
  `oracle`, `keycloak`, ..., which other projects' `deps.yaml` use too; prefer `build.yaml`.
- On every change, run `./gradlew build` (a few seconds). It compiles for Java 17, runs
  `spotlessCheck` (`./gradlew spotlessApply` formats) and builds `build/libs/dtm.war`. CI runs
  the same build.
- There are no tests yet: `build` runs none. Where a change's risk warrants a test, add JUnit 5
  unit tests in `src/test/java`, with the dependencies and `useJUnitPlatform()` in
  `build.gradle`, and check that `./gradlew build` runs them.
- Before opening a pull request, for a change to behavior or pages, rebuild and restart the app,
  since the container runs the image and not your working tree:
  `docker compose -f build.yaml up -d --build dtm`. Then check the change in a browser at
  `https://localhost:8443/dtm`, and look for errors in `docker compose -f build.yaml logs dtm`.
- Test data: `container/oracle/initdb.d` builds the schema, default data and settings when the
  Oracle container is created. `docker compose -f build.yaml down -v` deletes it; the next `up`
  rebuilds it. Schema and settings changes go there; there are no migrations, so give the SQL
  that existing databases need in the pull request.
- Accounts (Keycloak `test-realm`, password `password`): `jdoe` (`dtm-reviewer`), and `user1`
  to `user3` (`dtm-user`); `user3` also has `testlead`.
- Outside systems: Keycloak (login and the user directory), Oracle, Puppet Show (PDF and image
  export) and SMTP email run as containers; MailHog catches the email
  (`http://localhost:8025`). The JLab logbook has no stand-in: the image sets
  `LOGBOOK_SERVER_URL` to the real `https://logbooks.jlab.org`, the references page reads from
  it, and with the `LOGBOOK_ENABLED` setting on, incidents post entries to it, so leave that
  setting off. The SRM downgrade and NPES schedule settings point to JLab's servers too; leave
  them off. Never point the app at JLab's servers or production databases.
- UI changes: there are no browser tests or screenshot script; look at the page in a browser
  (forward the ports from the VM, or drive one) and say in the pull request what you checked.
- Compile for Java 17 (`options.release = 17`). Wildfly 37 provides the `providedCompile`
  dependencies; don't bundle them in the war. The smoothness weblib must stay `implementation`,
  in the war (JeffersonLab/smoothness#4).

<!-- BEGIN coding-agents shared rules 7.0.0 (generated: edit in acc/iac/docs/coding-agents) -->

## Shared rules

These rules are the same in every project that uses them. Where this file's other sections say
otherwise, follow those. On GitHub, "merge request" means "pull request", and "pipeline" means
the pull request's required checks.

### Checking your work

- Run the Development section's checks before you call a change done, fix what fails, and
  report what you ran and what it returned.
- Verify each change in proportion to its behavior and regression risk: add or update tests
  where the risk warrants it; meaningful existing coverage can suffice. For a bug fix, show the
  regression test failing without the fix and passing with it when practical, or explain why
  not. For documentation or formatting, check links, examples or formatting. Never skip or
  weaken a check merely to make it pass.
- Where tests are thin, characterization tests can record a baseline: existing behavior, not
  necessarily correct behavior. Put them in a test-only merge request when risk or review needs
  warrant it.
- Never use production data, and never commit secrets.

### Task authorization

- Use the request and authorization already given in the session; do not ask again for an
  authorized action. "Look around and suggest improvements" means investigate, run local
  checks and report, not implement or publish. "Fix this bug" authorizes the local changes and
  verification needed to finish it; ask before a material expansion of scope.
- Commit and push only when asked. "Create a merge request for issue #14" also authorizes
  implementing, verifying, committing, pushing the branch and opening the merge request, and a
  request to address its review authorizes the follow-up commits and pushes.
- A requested review authorizes checks and posting findings as comments; commit and push fixes
  only when asked.
- Releases, deployments, production data changes and settings changes in external systems need
  authorization for the action and the target environment; a request to implement a change or
  open a merge request does not give it. Never approve, merge, or enable auto-merge.
- Ask only when a consequential ambiguity remains; continue independent work meanwhile.

### Commits

- Commit as the agent's bot, never as a person: the repository-local `user.name` and
  `user.email` (never `--global`) must be the bot's whose token you push with, so the Git host
  links the commits to it and a squash commit shows the same name. In an agent VM the clone
  helpers set them; on GitLab, `glab api user` shows the bot's `name` and `username`, and its
  address is `<username>@noreply.<host>`.
- Imperative summary line, then a body explaining what changed and why. Name the issue the
  change is for: `Fixes #<issue>`, or `Part of #<issue>` if some of it stays open; work you were
  asked to do needs no issue.
- Trailers come last, in one paragraph with no blank line among them, since git reads trailers
  only from the last paragraph: the release trailers this file asks for, if any (such as
  `Changelog:`), then attribution. Every agent-written commit, including follow-ups and a
  reviewer's, names the agent and model in a trailer: keep the one your tool adds (such as
  `Co-Authored-By`), or add `Assisted-by: <agent>:<model>`. An agent VM's commit hook refuses a
  message that breaks either rule; elsewhere, check with
  `git log -1 --format=%B | git interpret-trailers --parse`.
- The squash commit message template uses the first commit's message: write it for `main`, put
  release trailers on it only, and keep it current for the whole change, adding any distinct
  agent/model attribution from later commits. Later commits omit release trailers but keep
  attribution.

### Branches and merge requests

- Start each task on a new branch from an up-to-date `main`, in your own clone or worktree, and
  target `main`. To build on a merge request still in review, branch from its branch, target it,
  and say so in the description; once it merges, check that yours now targets `main` (GitHub
  retargets it only if the merged branch was deleted).
- Label every merge request `source::ai`, make the person who reviews it both reviewer and
  assignee (ask for their username if you don't know it), set the active milestone where the
  project uses one, and delete the source branch on merge. An agent VM's `glab` wrapper adds
  these when you leave them out.
- Describe what changed, any deployment steps, and the checks you ran and could not run, in the
  project's merge request template where it has one. Add a short Decisions part: what the person
  questioned or changed during the session, which alternatives were dropped and why, and any
  second opinion that changed something.
- End every agent-prepared merge request, pull request or issue description with a plain-text
  `Prepared-by: <vendor> <agent>:<model identifier>` footer, such as
  `Prepared-by: OpenAI Codex:GPT-6`: the specific model identifier when known, otherwise the
  known model family, and `unavailable` only when neither is known; never guess. Preserve
  existing credits, including a tool's equivalent footer; on a substantive update, add yours if
  that vendor, agent and model are not credited yet; routine edits need none. Keep all
  attribution at the end, and in a release merge request under its own heading after "Release
  notes".
- After pushing, wait for your last commit's pipeline to finish before reporting the merge
  request ready: read a failed job's log and fix the cause, never skip or weaken a check, and
  report how it ended with its link, saying if it failed for a reason outside your change. On
  GitLab, `glab mr view <number> -F json | jq .head_pipeline` shows it; until its `sha` is the
  commit you pushed, your push's pipeline has not started.
- Before pushing to a merge request's branch, check that it is still open: commits pushed after
  it merged never reach `main`.
- For another GitLab project's issue or merge request, write `project#123` or `project!456` with
  no space when it resolves from the current context, otherwise the full namespace path or URL,
  and check the rendered link; `project #123` links the bare number to the current project.

### Issues

- For something outside your task (another project, code another agent is working on, a change
  that needs a decision), don't fix it in passing: offer to open an issue with what you found,
  the evidence and a suggested fix. File issues as the bot, labeled `source::ai` and assigned to
  the person you work with, with the project's labels for its kind where it has them: one
  `type::` label and, for a bug, one `bug-origin::` label (on GitLab,
  `glab api 'projects/:id/labels?include_ancestor_groups=true'` lists them); `effort::` only as
  an estimate, and `priority::` left to the person.
- When asked to plan work for other agents: check claims before filing, list the draft issues
  before filing them, make each self-contained with "Done when" and what not to do, say which can
  start now and which are blocked or share files, and ask before filing anything
  security-sensitive (file it confidential).

### Review

- When asked to address a review, reply in each thread with what you changed, and push content
  fixes as new commits; the reviewer resolves the threads. Only the author may reword the first
  commit's message, for its description, release metadata or attribution, following the Git
  workflow guide's "Responding to review": save the remote tip, keep the base, patches and
  order, check `git range-diff` and an empty `git diff`, push with an explicit
  `--force-with-lease`, and report the old and new tips.
- When asked to review a merge request, comment by default, one thread per finding; commit and
  push fixes only when asked, coordinating with the author if they are still working on the
  branch. Add one commit per recommendation on top of the author's, never rewriting theirs, with
  your attribution and no release trailers, then post one comment listing each commit, what you
  tested, that the author must pull, and any first-message update needed. Never approve, merge,
  or resolve threads.
- When the person corrects you on something any agent here should know, propose adding it to
  this file's project rules, or better a check that catches it. Keep one developer's
  preferences out of it.

<!-- END coding-agents shared rules -->

## Project rules

- PR checks: `build` from CI, and CodeQL. Wait for both before reporting a pull request ready.
- Changelog eligibility: unused. Types: unused.
- Breaking trailers: unused.
- Milestones and issue status: unused.
- Release trigger: changing `VERSION` on `main` creates the version tag and a GitHub release
  with `dtm.war`, publishes the demo Docker image, and deploys to JLab's acctest server.
  Release-only change: `VERSION` only, with `vX.Y.Z` as its commit message. Never change
  `VERSION` unless asked.
- Release notes: pull request description, including any upgrade steps (such as SQL for
  existing databases), for the maintainer to add to the generated release notes. Versions:
  [Semantic Versioning](https://semver.org/).
- Queue: the period's GitHub organization project. The agent cannot add items; report opened
  pull requests and issues so the person can add them.
