# Concepts

[User Guide](README.md) › Concepts

## Why record downtime

Downtime Manager (DTM) records equipment and system failures that interrupt or degrade the
scheduled program, so they can be counted and analyzed. The statistics show which categories,
systems and components fail, how often and for how long, and how long the machine takes to
recover. Management uses them to decide where to spend maintenance effort and money, so the
data is only as useful as it is accurate. Record downtime as it happens: an event that has to be
reconstructed later costs time and is less reliable.

DTM is not Beam Time Accounting (BTM). BTM accounts for the hours of beam delivered against the
program, for the Department of Energy metrics. DTM records each failure separately, by system,
so failures that happen at the same time are each recorded in full, even though beam was only
lost once. The two are independent: don't try to make DTM add up to the hours lost in BTM.

## Events and incidents

An **event** is a disruption of a program. Each event has a [type](#event-types), a title, a
start (the earliest incident's time down) and an end, its **time up**, when the program is
recovered: beam is back at the expected destination.

An **incident** is the failure of one system or component inside an event. Every event has at
least one incident. A single failure is one event with one incident; when a second system fails
while the first is still down, add a second incident to the same event, and so on. Each incident
has its own time down (when the component failed) and time up (when it was repaired and handed
back to Operations).

Only one event of each type can be open at a time, and events of the same type can't overlap in
time. Events of different types are independent and can be open at once: for example a CEBAF
Blocked event while Hall C has its own Hall C event.

### Restore time

The machine usually needs time to recover after the last repair: beam has to be re-established
and delivered again. That is the event's **restore** time: the parts of the event when no
incident is open, from the gaps between incidents to the time between the last incident's time
up and the event's time up. Closing every incident doesn't close the event; it stays open, and
restore time keeps growing, until you set the event's time up.

### Accounting examples

These examples, carried over from the original 2014 guidance, show how incidents record each
system's downtime separately.

- **One failure.** An RF zone fails for 2 hours of an 8-hour shift. Record one event with one RF
  incident: RF downtime is 2 hours, and BTM records 6 hours of beam.
- **Two failures at once.** An RF zone and a magnet box supply both fail for the same 2 hours.
  Record one event with two incidents, one RF and one magnet, each 2 hours. BTM still records 6
  hours of beam.
- **Overlapping failures.** An RF zone is down for 2 hours; later a magnet box supply fails for 1
  hour and a PSS drop lasts 2 hours, overlapping each other. Each gets its own incident, closed
  when that system is repaired: RF 2 hours, magnets 1 hour, PSS 2 hours. BTM records 4 hours of
  beam.

## Event types

The event type says which program was affected. An administrator can see the list on
**Setup › Types**; the default ones are:

| Type | Abbr. | Use for |
|---|---|---|
| CEBAF Blocked | BLK | Accelerator failures that block the CEBAF program. |
| CEBAF Degraded | DGD | Accelerator Division repairs that degrade the program but don't block it, such as a failure affecting one hall while CEBAF keeps delivering to others. Degraded events often run for a long time. |
| CEBAF Non-Program | NON | Failures that don't affect the CEBAF program. |
| CEBAF Tuning | TUN | Beam transport and tuning time; see [Beam transport and tuning](recording-downtime.md#beam-transport-and-tuning). |
| Hall A, B, C, D | HLA, HLB, HLC, HLD | Problems in an experimental hall that disrupt that hall's physics program. |
| LERF | LRF | The LERF program. |

The distinction between Accelerator Division and Physics Division downtime matters: they are
reported separately (it is the difference between BNA and BANU on experimenter time sheets). A
hall's own problem belongs in that hall's event, not in a CEBAF event. See
[Hall events](recording-downtime.md#hall-events).

Each event type allows only some categories, its **root categories**, so the component choices
in the incident form depend on the type. **Setup › Categories** lists them under "Type Roots".
With the default data:

| Type | Categories you can choose from |
|---|---|
| CEBAF Blocked | CEBAF, Other, Cryo, Facilities |
| CEBAF Degraded, CEBAF Non-Program | CEBAF, Other, Cryo, Facilities, Beam Transport |
| CEBAF Tuning | Beam Transport |
| Hall A, B, C, D | That hall's category, Hall Downtime |
| LERF | LERF, Other, Cryo, Facilities, Beam Transport |

## Categories, systems and components

What failed is recorded as a **component**. Components belong to **systems** (equipment with a
common function), and systems belong to **categories**, which form a tree: for example CEBAF ›
Magnets › Box Supply. Picking a component in the incident form fills in its system and category.
At JLab the lists come from SRM, so they match the other accelerator tools; they can't be edited
in DTM.

The special component **Unknown/Missing** is for when the failed component isn't in the list or
isn't known. DTM asks you to explain what it was, so the list can be corrected later.

**Alpha categories** are the categories that reports use as their top level, such as RF,
Magnets, Cryo, Beam Transport and Hall A: a flat set, none inside another, chosen to cover the
downtime once. They are listed on **Setup › Categories**.

**Repairer** groups (workgroups) record who repaired an incident. They have nothing to do with
permissions.

## Reviews

Every incident is reviewed twice. The reviews are covered in
[Reviewing incidents](review.md).

- **SME review**: the system's subject matter experts acknowledge the incident, give a root
  cause, or upload a Repair Assessment Report (RAR), depending on how long it lasted (its
  **review level**: Level Ⅰ under 30 minutes, Level Ⅱ under 4 hours, Level Ⅲ+ 4 hours or more).
- **OPR review**: the Operability reviewer checks the incident and records the repairer and
  solution.

## Trips

A **trip** is a short interruption caused by a Fast ShutDown (FSD) system fault. DTM records
trips automatically from the FSD system, and they are shown on the [Trips](trips.md) tab. A trip
that lasts more than 5 minutes should be recorded as a downtime event; most events start as
trips. DTM doesn't convert trips into events: you add the event yourself.

## Roles

| Role | Can |
|---|---|
| Anyone, without logging in | View the pages and reports. |
| Operator (any logged-in user) | Add, edit, close and delete events and incidents, until an incident has been OPR reviewed or its event closed more than 10 hours ago. Edit SME reviews and upload RARs. |
| Reviewer (`dtm-reviewer`) | Everything an Operator can, plus set the OPR reviewer, change reviewed incidents and old events, and edit the Operability review pages. |
| Admin (`dtm-admin`) | The Setup tab and the Activity Audit report. Admin doesn't include Reviewer. |

Reviewers choose to act as **Reviewer** or **Operator** from the menu next to their name at the
top of the page; they start as Reviewer when they log in. Everyone else is shown as Operator.
Role changes take effect the next time you log in.
