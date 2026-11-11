# Downtime Manager User Guide

Downtime Manager (DTM) records the failures that interrupt or degrade Jefferson Lab's
accelerator programs, and reports on them. Operators record downtime as **events** made of
**incidents**, one per failed system; subject matter experts and Operability review each
incident; and the reports turn the records into downtime, repair and availability statistics.
DTM also shows the Fast ShutDown (FSD) **trips** it gets from the FSD system.

At JLab, DTM is at [ace.jlab.org/dtm](https://ace.jlab.org/dtm). You can view it without logging
in; log in to make changes.

The [guide's website](https://jeffersonlab.github.io/dtm/) has a copy for each release; pick the
version the Help tab shows. On GitHub, this guide describes the `main` branch, which may be newer
than the version you are using.

## Contents

1. [Concepts](concepts.md): events, incidents, restore time, event types, categories, reviews,
   trips and roles.
2. [Recording downtime](recording-downtime.md): adding, updating and closing events and
   incidents, hall events, tuning and logbook entries. For operators.
3. [Reviewing incidents](review.md): review levels, the SME and OPR reviews, expert emails and
   Repair Assessment Reports (RARs). For experts and reviewers.
4. [Trips](trips.md): the FSD trip list.
5. [Reports](reports.md): the Reports, Operability and Beam Transport tabs, and how they count
   downtime.
6. [Administration](administration.md): the Setup tab and settings. For administrators.

## Tabs at a glance

| Tab | What it is for |
|---|---|
| **Open** | The events that are open now; start and update downtime here. |
| **Events** | All events, with filters. |
| **Trips** | FSD trips. |
| **Reports** | Downtime, trip and review statistics. |
| **Operability** | The weekly, monthly and annual repair and availability reports, and Joule. |
| **RAR** | Incidents with Repair Assessment Reports, and SAM RARs. |
| **Beam Transport** | Tuning (Beam Transport) incidents. |
| **Setup** | Administrators only: settings, experts and email. |
| **Help** | Version, contacts, documentation and a feedback form. |

## Quick start for operators

1. When something fails, go to **Open**. If an event of the right [type](concepts.md#event-types)
   is already open, select **Add Incident** on it; otherwise select **Add Event**.
2. Give it a title and summary, the time it went down, and the component that failed.
3. When the system is repaired and handed back, close the incident by setting its **Time Up**.
4. When beam is back where it should be, close the event by setting its **Time Up**.

See [Recording downtime](recording-downtime.md) for the details.

## Questions and changes

Use the feedback form on the Help tab, or contact the content and technical contacts listed
there. To suggest a change to this guide, open an issue or pull request on
[GitHub](https://github.com/JeffersonLab/dtm).

This guide replaces the 2014 "Downtime Manager Guidance" (Rev 1.3) by Randy Michaud,
Accelerator Operability; its definitions and guidance that still apply are included here.
