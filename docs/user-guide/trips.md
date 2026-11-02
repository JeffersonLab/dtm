# Trips

[User Guide](README.md) › Trips

The **Trips** tab lists Fast ShutDown (FSD) trips: short interruptions when the FSD system
removes beam because of a fault. DTM gets them from the FSD system; nobody enters or edits them
in DTM.

A trip that lasts more than 5 minutes should be recorded as a downtime event, with an incident
for the component that caused it (see [Recording downtime](recording-downtime.md)). DTM doesn't
do this for you. That is also why reports count only trips of 5 minutes or less as trip
downtime: longer ones are counted through their incidents.

## The list

Each row shows the faulted systems (joined with "+", or "<< PHANTOM >>" when no system is known,
with "(N)" when more than one device faulted and "^" while the trip is still open), when it went
down and how long it lasted. Hover over the time for seconds, and over the duration for the time
up.

Click a row to expand it:

- **Cause** and **Area**, which DTM works out from the faulted devices. Causes include a device
  category such as Magnets or Vacuum, "Hall", "Multiple/Other", "Unknown/Missing", and finer ones
  such as "RF (C25/50)", "RF (Multi/Other)", "MPS (BLM)" or "Dump (Insert.)". Areas are
  Accelerator, Accelerator (RF), Hall A to D, Multiple or Unknown.
- **Trip ID**.
- **Machine State**: the accelerator's and each hall's state when it tripped.
- **Faulted FSD Channel**: the node, channel and path, with each faulted device's category,
  system, CED type and name, whether it is confirmed, and its region.

## Filtering

Open the filter with **Choose...**:

- **Date Range**: trips that started in the range.
- **Min Trip Duration**, **Max Trip Duration**, in seconds, minutes or hours.
- **Machine State**: the accelerator state (PHYSICS, OFF, DOWN, ACC, RESTORE, STUDIES) and each
  hall's state (OFF, DOWN, TUNE, BANU, UP).
- **Cause**, **Area**, **System**, **CED Type**, **CED Name**: text, with `%` as a wildcard.
- **Max CED Types Per Trip**, **Max Devices Per Trip**, the FSD **Node** and **Channel #**, and
  **Trip ID**.

Show 10, 25 or 100 trips per page. **Export** downloads the list as **CSV** (one row per trip) or
**CSV (Exploded)** (one row per faulted device). The export doesn't apply the Area and Machine
State filters, so it can have more rows than the page.

The trip reports are **Reports › FSD Summary** and the trip counts in **Reports › Downtime
Summary**; see [Reports](reports.md).
