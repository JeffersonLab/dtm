# Administration

[User Guide](README.md) › Administration

This page is for DTM administrators: users with the `dtm-admin` role, who see the **Setup** tab.
Being an administrator doesn't make you a reviewer; that is the `dtm-reviewer` role. See
[Roles](concepts.md#roles).

## Setup tab

| Page | For |
|---|---|
| **Settings** | The app's settings; see below. Select a row to edit its value. |
| **Directory Cache** | DTM caches users' names, emails and roles, and clears the cache every night at midnight. Clear it, or look up a user or role, after a change in the directory. Users must log out and in again for a role change to apply. |
| **Types** | The event types, read-only. |
| **Categories** | The category and system tree, the alpha categories and each event type's root categories, read-only. |
| **Expert List** | Add or remove a system's subject matter experts (SMEs). Choose the system with **Choose...**. |
| **Email** | Whether the daily expert emails are on, and **Preview** or **Send Email Now** to one SME. |

Event types, categories, systems, components and repair groups can't be changed in DTM. At JLab
the categories, systems, components and groups come from SRM.

### Alpha categories

Many reports use the alpha categories as their top level. They must cover all downtime once:
none can be inside another, and none can be left out. Including a top-level category would rule
out every category under it, and a category that has systems beside it can't be one, since its
parent is needed to count those systems. The **Beam Transport** category must be an alpha
category: the NPES schedule allows time for beam transport (tuning), and the Joule report uses
it.

### Experts

When an incident is created or its system changes, the system's experts become the incident's
SMEs. Changing the expert list doesn't change incidents that already exist.

## Settings

**Setup › Settings** lists the settings with their descriptions. Some changes can break the app
or the Settings page itself, so that they can only be undone in the database. Changes made in the
database directly take effect after **Refresh Cache** or a redeploy, and `IP_READ_FILTER_ENABLED`
and `IP_READ_URL_PATTERN` always need a redeploy.

| Setting | Effect |
|---|---|
| `NOTIFICATION_ENABLED`, `NOTIFICATION_MESSAGE`, `NOTIFICATION_LINK_NAME`, `NOTIFICATION_LINK_URL` | The banner at the top of every page. |
| `HELP_CONTENT_CONTACT`, `HELP_TECHNICAL_CONTACT` | The contacts on the Help tab. |
| `HELP_DOC_CSV` | The Help tab's documentation links: `URL\|Label` items separated by commas (neither can contain a comma). |
| `ADMIN_ROLE_NAME` | The role whose members are listed on the Help tab and get the feedback form's emails. |
| `EMAIL_ENABLED` | Turns on the daily expert emails and the Help tab's feedback emails. |
| `EMAIL_TESTING_ENABLED` | Sends emails to the members of the `testlead` role instead, for testing. If no member of that role has an email address, expert emails are not sent. |
| `EMAIL_EXPERT_CC_LIST` | Addresses copied on every expert email. |
| `EMAIL_SENDER_ADDRESS` | The sender of feedback emails, and the sender and from address of expert emails. |
| `EMAIL_DOMAIN_NAME` | Added to an expert's username to make the address their emails go to, such as `@jlab.org`. |
| `LOGBOOK_ENABLED`, `LOGBOOK_LIST` | Logbook entries for incidents, and the logbooks they go to; see [Logbook entries](recording-downtime.md#logbook-entries). |
| `SRM_DOWNGRADE_ENABLED`, `SRM_DOWNGRADE_URL` | The **Downgrade Readiness** link on incidents of systems that SRM tracks. |
| `NPES_SCHEDULE_ENABLED`, `NPES_SCHEDULE_URL` | The NPES schedule's budgeted hours on the Joule report. Off, they are 0. |
| `IP_READ_FILTER_ENABLED`, `IP_READ_ALLOWLIST_PATTERN`, `IP_READ_URL_PATTERN` | Whether users outside the allowed addresses must log in to view pages. |
| `SMOOTHNESS_CDN_ENABLED`, `SMOOTHNESS_SERVER` | Where the pages' shared scripts and styles come from. |

Server settings, such as where RAR files are stored (`RAR_DIR`), the logbook server and the
bulletin board for escalations, are environment variables; see the
[README](../../README.md#configure).

## Help tab

The Help tab shows the version, the contacts, the administrators, the `HELP_DOC_CSV` links and a
feedback form, which emails the administrators. To link this guide from it, add an item such as
`https://github.com/JeffersonLab/dtm/blob/main/docs/user-guide/README.md|User Guide` to
`HELP_DOC_CSV`.
