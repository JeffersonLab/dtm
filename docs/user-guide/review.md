# Reviewing incidents

[User Guide](README.md) › Reviewing incidents

Each incident is reviewed twice:

- the **SME review**, by the subject matter experts (SMEs) for the failed system, and
- the **OPR review**, by Operability.

The review status shows on each event's row (an SME icon and an OPR icon: reviewed, partly
reviewed or not reviewed) and in each incident's **Review** column (SME: Yes/No, OPR: Yes/No).
Select the **\*** next to "Review:" to see the full review.

## Review levels

An incident's review level depends on how long it lasted, and decides what the SME review
needs. The incident's row shows the level as "Unknown" until the incident is closed; reports
count open incidents as Level Ⅲ+ and not reviewed.

| Level | Incident duration | The SME review is done when |
|---|---|---|
| Level Ⅰ | under 30 minutes | **Acknowledged** is Yes |
| Level Ⅱ | 30 minutes to under 4 hours | a **Root Cause** is given |
| Level Ⅲ+ | 4 hours or more | a Repair Assessment Report (RAR) is uploaded |

The expert email describes what each level involves:

- **Level Ⅰ**: short repairs (5–30 minutes). Group leader or SME review: check a box. Due in 2
  days.
- **Level Ⅱ**: a single system, more than 30 minutes of repair. Review and a root cause
  statement. Due in 2 days.
- **Level Ⅲ**: a 4-hour escalation, or at the Director of Operations' discretion. A root cause
  memo using the Level Ⅲ template, and a 3–4 minute report at the next Weekly Summary Meeting.
- **Level Ⅳ**: a program change, a safety issue, a compounded event, or at the Director of
  Operations' discretion. A Repair Investigation Team, using the Level Ⅳ template, within 3
  weeks.

DTM itself doesn't tell Level Ⅲ and Ⅳ apart: both are Level Ⅲ+. Reports count an incident
with a RAR as reviewed whatever its level. The
[RAR Procedure](https://ace.jlab.org/cdn/doc/dtm/RARProcedure.pdf) has the details.

## SME review

### Who the SMEs are

Each system has a list of experts, kept by the DTM administrators on **Setup › Expert List**
(and shown on **Reports › Expert**). When an incident is created, or its system is changed, the
system's experts become the incident's SME **Reviewer(s)**. To assign other experts, edit the
incident and change **Reviewer(s)** (usernames separated by spaces) on the **Explained** tab.
Whenever the SMEs change, the incident's acknowledgement goes back to No.

### Expert email

Where email is turned on, each weekday at 9:55 DTM emails every SME who has closed incidents
that aren't acknowledged from the last 24 hours (72 hours on Mondays). The email lists the
incidents with links, a **DTM-RAR Review** link to the SME's unacknowledged incidents, and the
action level table above. Administrators can preview it, or send it to one person, from
**Setup › Email**.

### Doing the review

1. Find the incident: follow the link in the email, or on **Events** filter by **SME** and
   **Expert Acknowledged**.
2. Select **Edit SME Review** on the incident's row (or on its incident page).
3. Fill in what the incident's level needs:
   - **Acknowledged (Level Ⅰ)**: Yes, or **Reassign** if it was assigned to the wrong expert.
   - **Root Cause (Level Ⅱ)**: what caused the failure, up to 512 characters.
   - **Repair Assessment Report [RAR] (Level Ⅲ+)**: choose the report file (PDF, Word or text, up to 10 MB).
     It uploads as soon as you choose it; uploading again replaces it.
4. Select **Save**.

Giving a root cause also sets Acknowledged to Yes; uploading a RAR doesn't, so set it to Yes
as well, or the incident stays on your list of unacknowledged incidents. Reassign only marks the
incident; nobody is notified, so change its **Reviewer(s)** or tell Operability who the right
expert is.

## OPR review

Operability reviewers (the `dtm-reviewer` role, acting as **Reviewer**) review each incident's
record: they correct it where needed, then select **Edit Incident**, open the **Explained** tab
and fill in:

- **Reviewer**: the reviewer's username (**Me** fills in yours),
- **Repairer**: the groups that repaired it,
- **Solution**: how it was fixed.

Incidents can be OPR reviewed while their event is still open, which helps with long degraded
events.

Once an incident has an OPR reviewer, operators can no longer change it or its event; reviewers
still can. Reviewers can also change events that closed more than 10 hours ago. Changes made as
Reviewer don't create logbook entries; switch to **Operator** in the menu next to your name if
you want them.

The Operability tab's weekly and monthly repair pages have reviewer-only editing too; see
[Reports](reports.md#operability).

## RAR tab

The **RAR** tab lists the incidents that have a Repair Assessment Report, with their hours, the
problem and component, the root cause, the report's **Download** link and upload date, and the
SMEs with whether the incident is reviewed. It opens on the previous month's CEBAF Blocked
incidents; change that with **Choose...**, and sort by duration, time down or upload date.

Its buttons open the **Procedure** and the **Level Ⅲ Template** and **Level Ⅳ Template**
documents.

### RARs for SAM periods

**Add SAM RAR** records a RAR that doesn't belong to a downtime incident, such as one for work
during a scheduled accelerator maintenance (SAM) period. It creates a closed CEBAF Blocked event
with one Unknown/Missing incident to hold the report. That event is counted like any other, so
its hours appear in the CEBAF Blocked downtime reports.

To add one:

1. Select **Add SAM RAR**, enter a **Title** and **Time Down**. **Time Up** defaults to 4 hours
   after the time down.
2. Select **Next**, choose the report file, then select **Done**.

These events must not overlap other CEBAF Blocked events. If the exact time doesn't matter, use
a 4-hour block starting at midnight on the first day of the SAM, and put further SAM RARs on
different days. Operators can only add them for the last 10 hours; a reviewer can add older
ones.

## Finding incidents that need review

- **Reports › Root Cause** lists incidents with their review level, root cause and SMEs, filtered
  by **All Incidents**, **Not Reviewed** or a single level.
- **Events**, filtered by **SME** and **Expert Acknowledged**, shows one expert's incidents.
