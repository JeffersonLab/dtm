# Recording downtime

[User Guide](README.md) › Recording downtime

This page is for operators: how to add, update and close events and incidents. Read
[Concepts](concepts.md) first for what events, incidents and event types are.

## What to record

- Record downtime once hot checkout is complete and a beam program is set (beam authorized,
  beam commissioning, and so on). Failures during a scheduled accelerator maintenance (SAM)
  period aren't recorded.
- Record failures that affect the scheduled program, as they happen.
- Beam studies are part of the scheduled program and are accounted for in BTM, not as downtime.
- If the program is changed because of a failure, keep the event open until the repair is done
  and the original program resumes, or until Operability says to close it.
- An FSD trip that lasts more than 5 minutes should be recorded as an event. See
  [Trips](trips.md).

## Where to work

- **Open** lists the events that are open now. This is where you start and update downtime
  during a shift. Its links open the current and previous shift's incident summaries and the
  wall display.
- **Events** lists all events, newest first, 10 to a page, filtered by date range, type, Beam
  Transport, SME, acknowledgement, event ID or incident ID (open the filter with **Choose...**).

Click an event's row to expand it. The header shows the type, title, time down and duration
(highlighted after 2 hours and again after 4), "(N)" when it has more than one incident, "^"
when it is still open, and icons for whether its incidents are SME and OPR reviewed. Expanded,
it shows a timeline of its incidents, the restore time and a table of the incidents.

You must be logged in to make changes.

## Adding an event

1. On **Open** (or **Events**), select **Add Event**.
2. On the **Observed** tab, under **Event Information**, enter the event **Title** and choose
   the **Type**. Leave **Time Up** empty for an event that is still going on.
3. Under **Incident Description**, the incident **Title** is the event's title while **Same as
   Event** is checked. Enter a **Summary** of what was observed and, if there is one, the
   **ePAS** permit number.
4. Under **Incident Period**, **Time Down** is filled in with the current time; correct it to
   when the failure happened. Leave **Time Up** empty while the incident is open.
5. Under **Incident Cause**, type at least two letters of the **Component** and pick it from the
   list. To browse instead, check **Filter by Category/System**, choose a category and system,
   and select **▼** to list the system's components.
6. Select **Save**.

Times are entered as `DD-MMM-YYYY hh:mm` in 24-hour time, such as `02-Nov-2026 14:05`; use the
date picker or the **Now** button. Times can't be in the future.

The form checks a few things before it saves:

- Only one event of each type can be open. If an event of that type is already open, add an
  incident to it instead.
- Events of the same type can't overlap in time.
- If you choose **Unknown/Missing** as the component, you are asked to explain what component
  failed.
- For a hall event you are reminded to set BOOM to BNR mode, if applicable.
- Choosing Controlled Access or End Station Procedure in a CEBAF Blocked event asks you to
  confirm, since those belong in hall events.

To record an event that is already over, fill in the event's **Time Up** and the incident's
**Time Up**. Operators can only add events that ended in the last 10 hours; ask a reviewer for
older ones.

## Adding an incident

When another system fails during an open event, add an incident to that event rather than
starting a new event of the same type.

1. Expand the event and select **Add Incident**.
2. Enter the incident's title, summary, time down and component as for a new event.
   **Append Incident Title** adds the incident's title to the event's title
   ("Original + New"); uncheck it to keep the event's title.
3. Select **Save**.

## Updating and closing incidents

Select **Edit/Close Incident** on the incident's row to correct it or close it (once it is
closed, the button is **Edit Incident**; clear its Time Up to reopen it). To close it, set
its **Time Up** to when the system was repaired and handed back to Operations.

The **Explained** tab holds the [review](review.md) fields. Operators can fill in the
**Repairer** groups and the **Solution**; only a reviewer can set the OPR **Reviewer**.

The subject matter experts (SMEs), in **Reviewer(s)** on the **Explained** tab, come from the
expert list for the incident's system. They are set when the incident is created, and again
whenever its system changes; you can change them there too.

## Closing an event

When the program is recovered, select **Edit/Close Event**, set the event's **Time Up** and
select **Save**. Any incidents still open are closed at the same time. The time up can't be
earlier than the last incident's time down or time up.

Once the event is closed the button is **Edit Event**. To reopen the event, clear its **Time
Up**; you can't while another event of the same type is open. You can also change an event's
title and type, as long as it doesn't then overlap another event of the new type.

## Deleting

**Delete Incident** deletes an incident; deleting the last one deletes its event too. **Delete
Event** deletes the event with all its incidents. Both ask you to confirm.

## When you can't make changes

Operators can't edit, add to or delete:

- an incident that has been OPR reviewed, or an event that contains one;
- an event that was closed more than 10 hours ago.

The buttons are hidden in those cases; ask a reviewer. (The error message for the second case
says "over 4 hours", but the limit is 10 hours.)

## Hall events

A problem that is unique to an experimental hall and disrupts its program is recorded as that
hall's event, not as a CEBAF event. Each hall, CEBAF and LERF have their own events, which are
independent of each other and can be open at the same time.

For a hall problem:

1. Add an event with the hall's type, such as **Hall D**.
2. Choose a component from the hall's category or from **Hall Downtime**, such as Controlled
   Access, End Station Procedure or Hall Downtime (Unk/Missing).

If the accelerator also fails, while the hall event is open or not, record that in a separate
CEBAF event. Don't record problems in several halls as incidents of one CEBAF event: give each
hall its own event.

## Beam transport and tuning

Program interruptions caused by beam quality rather than a hardware failure, which used to be
called "tuning", "tune time" or "unknown", are recorded in the **Beam Transport** category.
Examples are running the locks to fix orbit shifts, cleaning up the orbit after beam loss
monitor trips, ion chamber trips due to drift, unexplained beam instabilities, and hall tuning of
beam positions.

- Use the **CEBAF Tuning** type, which allows only Beam Transport components.
- Pick the component that is closest to what is happening, such as Orbit, Optics, Injector or
  Beam Instability.
- If the instability is caused by a known hardware component, record it as a failure of that
  component instead.

Tuning is reported separately. The **Beam Transport** tab reports Beam Transport incidents,
and most reports can include them or leave them out. The Joule report's "Tuning, Setup, &
Restore" counts CEBAF Tuning events only.

## Logbook entries

Where the logbook integration is turned on (the `LOGBOOK_ENABLED` setting):

- DTM makes a logbook entry when an operator creates or closes an incident, with its summary,
  times and cause, and a link back to DTM. Actions taken as Reviewer make no entries.
- The incident table has a **Log Entries** column listing entries that reference the incident.
  **Create New Log Entry** opens a new logbook entry already referencing it (`dtm:<incident
  ID>`), and **View Interval ± 1Hr** opens the logbook from an hour before the incident to an
  hour after.

Make logbook entries for an incident from DTM, so they are tagged with it: anyone (operators,
technicians, engineers, managers) can add to the incident's record that way, and the entries
form its timeline.

## Other tools

- **Incident page.** Each incident has a page with all its details, at
  `/dtm/incidents/<incident ID>`; select the **\*** next to its ID in the incident table to copy
  the link. Events have links too, at `/dtm/events/<event ID>`.
- **Wall display.** `/dtm/wall` is a full-screen list of open events for a control room screen.
  It updates every minute.
- **Downgrade Readiness.** Where the SRM integration is on, incidents on systems that SRM tracks
  have a **Downgrade Readiness** link, which opens an SRM signoff for that system and component
  marked Not Ready.
- **Escalation.** Where it is set up, DTM posts to the control room bulletin board when an event
  has been open for 2 hours, and again at 4 hours.
- **Shift summaries.** On **Open**, **Current Shift Summary** and **Previous Shift Summary** open
  the Incident Downtime report for the shift (Owl 23:00–07:00, Day 07:00–15:00, Swing
  15:00–23:00).
