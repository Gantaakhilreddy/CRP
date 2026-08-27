# CampusOS schema (MySQL / JPA)

Source of truth is JPA entities with `ddl-auto=update`. All booking availability is computed from these tables — never hardcoded.

## Core graph

```
users ── bookings ── booking_resources ── resources ── floors ── buildings
              │                               │
              ├── booking_approvals           ├── resource_facilities ── facilities
              ├── booking_equipment           └── resource_types
              └── check_ins
```

## Integrity

| Table | Constraints / indexes |
| --- | --- |
| users | unique email |
| buildings | unique code |
| resources | unique code, unique qr_token (legacy unused), indexes on floor, building, type, status |
| bookings | indexes on user, date, status, (date,start,end) |
| booking_resources | unique (booking_id, resource_id) |
| favorites | unique (user_id, resource_id) |

Overlapping bookings are prevented in `BookingService.create` with pessimistic row locks on each resource plus a time-range query. MySQL cannot express range exclusion constraints; the lock + revalidation is the authority.

## Status

Resource live status is derived: maintenance windows, blocks, and overlapping active bookings (`PENDING_*`, `CONFIRMED`, `CHECKED_IN`).
