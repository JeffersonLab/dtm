# Reports

[User Guide](README.md) › Reports

DTM's reports are on three tabs: **Reports** for downtime and trip statistics, **Operability**
for the regular availability and repair reviews, and **Beam Transport** for tuning. Anyone can
view them without logging in, except Activity Audit, which is for administrators.

These numbers are for understanding system performance. They aren't the official DOE
availability figures, which come from BTM.

## Using the report pages

- **Choose...** opens a report's filters; **Apply** runs it. Some reports also have **Reset**
  and **Clear** links.
- **Date Range** offers presets (fiscal years and their quarters, calendar years, months, weeks
  starting Wednesday, crew chief shifts, the past 3, 7 or 10 days, yesterday, today) and
  **Custom...**. The presets' days start at 7:00. Custom start dates are inclusive and end
  dates exclusive. Most reports default to the past 7 days.
- The address bar always holds the report's full settings, so you can bookmark it or send the
  link. Many reports share your last choices during your session, so a date range you pick on
  one carries over to the next.
- **Full Screen** hides the page's menus. **Export** offers some of: **Image** (a PNG of the
  page), **Print** (a print-friendly page), **Excel** and **CSV**.
- Charts can usually be shown as **Bar**, **Pie**, **Pareto** or **Table**, of downtime, count or
  mean time to recover. Select a label or bar to drill down to the next report.

### How downtime is counted

- Durations are **bounded** by the date range: only the part of an event or incident inside the
  range counts. Hover over a duration to see the full, unbounded one. Open events and incidents
  count up to now.
- **Event downtime** is the time from an event's first incident down to the event's time up.
  It is made up of **suspend** time, when at least one incident is open, and **restore** time,
  when none is.
- **Incident downtime** adds up every incident, so incidents at the same time are each counted
  in full.
- **Non-overlapping** downtime merges overlapping incidents in the same system (or category) and
  event type, so that time is counted once.
- **Trip downtime** counts FSD trips of 5 minutes or less, outside SAM (longer trips should be
  events). See [Trips](trips.md).
- **Program time** is the scheduled program time from the crew chief time sheets in BTM: physics,
  studies, restore, accelerator and down time. Uptime, MTBF and availability are worked out from
  it, so they are shown only for the CEBAF Blocked type.
- **MTTR** (mean time to recover) is downtime divided by the number of failures; **MTBF** (mean
  time between failures) is uptime divided by the number of failures; **availability** is uptime
  as a percentage of program time.
- **Beam Transport** incidents are the ones in the Beam Transport category. Most reports can
  exclude them, or show only them.

## Reports tab

| Page | Shows |
|---|---|
| **Summary** | The headline numbers for one event type: event, suspend, restore and incident downtime, counts, MTTR, non-overlapping downtime and trips. For CEBAF Blocked it adds program time, uptime, MTBF, failure rates, availability and the losses due to events and trips. Select the **\*** on a row for its definition. |
| **Root Cause** | Counts and hours of incidents by [review level](review.md#review-levels) and how many aren't reviewed, then the incidents for the column you choose, with their root cause, RAR and SMEs. Defaults to the previous month's CEBAF Blocked incidents. |
| **Category Down** | Incident downtime, count and MTTR by alpha category, with or without merging overlaps. For CEBAF Blocked alone it adds program time, uptime, MTBF, failure rate, availability and loss. Excel and CSV export. |
| **System Down** | The same by system, optionally for one category. Excel export. |
| **Component Down** | Incident downtime, count and MTTR by component, optionally for one system, with the same CEBAF Blocked columns. |
| **Incident Down** | Individual incidents, longest first, filtered by duration, type, system, repair group or component, with totals split into overnight (19:00–07:00) and daytime. Excel export, and a **Shiftlog** export of the list as a web page. |
| **Event Down** | Events with their downtime, restore time and incident counts. Excel export. |
| **Event Timeline** | The events in a period on a timeline, one row per event type. |
| **Repair Summary** | Repair hours or incident counts over time, by repair group. CSV export. |
| **FSD Summary** | FSD trip counts, rates, lost hours and average durations over time, by cause or area. Rates can be per program hour or per hour of the period. CSV export. |
| **Activity Audit** | Administrators only: who changed which events and incidents, when and from which computer, with each version's values. |
| **Expert** | The SMEs for each system, optionally for one category. |

## Operability

The Operability tab has the reports used in the weekly and monthly reviews. Most of them cover
the CEBAF Blocked type.

| Page | Shows |
|---|---|
| **Weekly Repair** | A week's top incidents (5 by default) with their solution, system and component, repairer, and how often the component failed in the last 4 weeks. Reviewers can edit an incident from here with **Edit Selected**. |
| **Weekly Cause** | The Level Ⅲ+ incidents whose RAR was uploaded during the week. |
| **Monthly Avail.** | The monthly availability report: overall availability against the goal and the previous month, trips and repairs, the top repairs, repairs by category, and repairs by category and day. Reviewers can edit the goals and the notes. |
| **Run Compare** | Accumulated downtime of several runs (date ranges you add and label), side by side. |
| **Trend** | Downtime, uptime and availability overall, and downtime, counts, MTTR, MTBF and availability for trips, for events and per category, in bins of a day to a year. Excel export. |
| **Annual Repair** | A year's CEBAF Blocked repair hours by category, stacked by month, with each month's program hours. |
| **Joule** | Reliability for the Joule metrics, from DTM's downtime and BTM's hours; see below. CSV export. |

### Joule

The Joule report combines BTM's hours with DTM's CEBAF Blocked and CEBAF Tuning downtime:

- **Research** is physics time less tuning and physics downtime, plus any **Quality Hours**
  allowance.
- **Tuning, Setup, & Restore** is CEBAF Tuning downtime plus BTM's restore and accelerator time.
- **Delivered** is research, studies, and tuning, setup and restore.
- **Unscheduled Failures** are CEBAF Blocked downtime less the **Maintenance Hours** allowance.
- **Reliability** is delivered time as a percentage of delivered time plus failures.
- **Budgeted** hours come from the NPES schedule, times the **Budget Scaler**. The report also
  gives delivered as a percentage of budgeted, and research as a percentage of delivered time
  plus failures.

Allowances are spread evenly over the bins. The report can be a table or a chart: reliability
per bin, reliability with its accumulated average (weighted by scheduled hours, so the last point
is the whole period's reliability), delivered against budgeted progress, or delivered against
failures. The reliability charts mark the DOE minimum of 80%.

## Beam Transport

The Beam Transport tab reports only Beam Transport (tuning) incidents.

| Page | Shows |
|---|---|
| **Weekly Tune** | A week's top tune incidents, like Weekly Repair, with the total tune time. Excel export. |
| **Tune Incidents** | Tune incidents in a period, newest first, filtered by type and component. Excel export. |
| **Tune Comparison** | Which Beam Transport components took the most tune time. |
