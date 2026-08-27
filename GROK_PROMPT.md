# MASTER BUILD PROMPT

## AI-POWERED COLLEGE RESOURCE BOOKING & CAMPUS MANAGEMENT PLATFORM

You are a **senior full-stack engineer, software architect, UI/UX designer, database architect, AI engineer, and hackathon product developer**.

Build a complete, production-quality, visually impressive **College Resource Booking Portal** from scratch.

The application must feel like a **next-generation digital campus platform**, not a simple CRUD application.

The system should allow students, professors, faculty and administrators to visually explore the college campus, buildings, floors and resources, check real-time availability, make bookings, obtain approvals, check in using QR codes, manage resources, and analyze campus utilization.

---



# 1. CORE TECHNOLOGY STACK

Use exactly this stack.

## Frontend

* React.js
* JavaScript only
* Vite
* React Router
* Axios
* Tailwind CSS
* Framer Motion
* Lucide React icons
* Recharts
* FullCalendar or another high-quality calendar library
* QR code generation library
* QR scanning library

DO NOT use TypeScript.

---

IMAGES:
USE Image C:\Users\ganta\Downloads\images.jpg is my college photo
College Architecture for visual representation feature C:\Users\ganta\Downloads\Untitled.png

## Backend

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* Spring Security
* JWT
* Maven
* Bean Validation

---

## Database

Use:

* Local MySQL / SQL
* JPA/Hibernate
* Foreign keys
* Indexes
* Constraints
* Transactions

The database must be the source of truth.

---

## AI

Use:

**Groq API**

The Groq API key will be stored only in:

```text
backend/.env
```

Support:

```text
GROQ_API_KEY=
```

Never expose the Groq API key to React.

Never call Groq directly from the browser.

All AI requests must go:

```text
React
 ↓
Spring Boot
 ↓
Groq API
 ↓
Spring Boot validation
 ↓
React
```

---

# 2. IMPORTANT DEVELOPMENT RULE

Build the project completely from scratch.

Do not create a fake prototype.

Do not use mock APIs for core functionality.

Do not hardcode booking availability.

Do not hardcode dashboard statistics.

Do not create buttons that do nothing.

Everything visible in the UI should either:

* work,
* navigate to a working feature,
* or clearly indicate that it is intentionally disabled.

---

# 3. PROJECT VISION

The application should combine:

### Campus Digital Twin

Visual representation of:

```text
College
 ↓
Buildings / Blocks
 ↓
Floors
 ↓
Rooms
 ↓
Labs
 ↓
Seminar Halls
 ↓
Library
```

with:

### Resource Booking

```text
Search
 ↓
Availability
 ↓
Selection
 ↓
Booking
 ↓
Approval
 ↓
Confirmation
 ↓
QR Check-in
 ↓
Check-out
```

and:

### AI Assistant

```text
Natural Language
 ↓
AI Intent
 ↓
Resource Search
 ↓
Availability
 ↓
Recommendation
 ↓
Booking Confirmation
```

and:

### Management Analytics

```text
Bookings
 ↓
Utilization
 ↓
Campus Analytics
 ↓
Optimization
```

---

# 4. COLLEGE CAMPUS IMAGE

I will provide an image showing the actual college campus/building structure.

Use this image as the visual reference for the application.

IMPORTANT:

Do not simply display the image.

Convert the structure into an interactive digital campus experience.

Identify the buildings/blocks and their names from the image where readable.

If a building name is unclear, create a configurable placeholder that the administrator can rename.

The campus interface should visually resemble the provided college structure.

---

# 5. CAMPUS EXPLORER

Create the main:

```text
/campus
```

page.

Display an interactive campus representation.

Example:

```text
                COLLEGE CAMPUS

       ┌───────────────┐
       │    BLOCK A    │
       └───────────────┘

 ┌────────────┐       ┌────────────┐
 │  BLOCK B   │       │  BLOCK C   │
 └────────────┘       └────────────┘

        ┌────────────────────┐
        │   CENTRAL BLOCK    │
        └────────────────────┘
```

Each building must be clickable.

When hovered:

* highlight building
* show building name
* show number of resources
* show current availability

When clicked:

```text
Campus
 ↓
Building
```

---

# 6. BUILDING EXPLORER

Create:

```text
/buildings/:id
```

Display:

* Building name
* Building code
* Description
* Building image
* Number of floors
* Number of classrooms
* Number of labs
* Seminar halls
* Library if applicable
* Available resources
* Current occupancy

Show floor cards.

Example:

```text
BLOCK A

┌────────────────┐
│ GROUND FLOOR   │
│ 5 Rooms        │
│ 2 Labs         │
│ 1 Hall         │
│ 7 Available    │
└────────────────┘

┌────────────────┐
│ FIRST FLOOR    │
│ 5 Rooms        │
│ 2 Labs         │
│ 1 Hall         │
│ 5 Available    │
└────────────────┘
```

---

# 7. COLLEGE BUILDING STRUCTURE

Normal academic blocks should support:

* At least 5 classrooms per typical floor
* At least 2 laboratories per typical floor
* 1 seminar hall per block

This should be configurable in the database.

Do not hardcode room counts into React.

---

# 8. CENTRAL BLOCK

The Central Block has special rules.

Do NOT create normal classrooms in the Central Block.

Use:

```text
CENTRAL BLOCK

1st Floor
└── Seminar Hall

2nd Floor
└── Library

3rd Floor
└── Library
```

Library must have resource type:

```text
LIBRARY
```

Library attributes:

* Capacity
* Study seats
* Reading areas
* Opening hours
* Current occupancy
* Facilities

---

# 9. FLOOR MAP

This is one of the most important features.

When a user opens a floor, display a visual representation of the floor.

Example:

```text
                    FLOOR 2

┌──────────┬──────────┬──────────┐
│ ROOM 201 │ ROOM 202 │ ROOM 203 │
│   🟢     │   🔴     │   🟢     │
└──────────┴──────────┴──────────┘

┌────────────────┐ ┌────────────────┐
│    LAB 204     │ │    LAB 205     │
│      🟢        │ │      🟡        │
└────────────────┘ └────────────────┘

┌────────────────────────────────────┐
│            SEMINAR HALL            │
│                  🟢                │
└────────────────────────────────────┘
```

Every room/resource should be clickable.

---

# 10. DIGITAL FLOOR MAP ENGINE

Store visual positions in the database.

Resource fields:

```text
positionX
positionY
width
height
rotation
```

The frontend renders resources based on these values.

This allows administrators to create and modify floor layouts.

---

# 11. FLOOR MAP EDITOR

Create an admin-only visual floor editor.

Admin can:

* Add room
* Add lab
* Add seminar hall
* Move resource
* Resize resource
* Rename resource
* Change resource type
* Save layout

Prefer drag-and-drop.

The floor map should be stored in SQL.

---

# 12. RESOURCE TYPES

Support:

### Classroom

* Capacity
* Department
* Projector
* Smart board
* AC
* Wi-Fi

### Laboratory

* Capacity
* Department
* Computers
* Equipment
* Software
* Lab facilities

### Seminar Hall

* Capacity
* Projector
* Audio
* Microphones
* AC
* Stage

### Library

* Capacity
* Study seats
* Reading area
* Opening hours

### Auditorium

* Capacity
* Audio
* Stage
* Projector

### Examination Hall

* Capacity
* Seating arrangement

### Sports Facility

* Type
* Capacity
* Equipment

### Equipment

* Projectors
* Laptops
* Cameras
* Microphones
* Speakers

Allow administrators to create custom resource types.

---

# 13. RESOURCE STATUS

Use:

```text
AVAILABLE
BOOKED
MAINTENANCE
BLOCKED
OUT_OF_SERVICE
PENDING
```

Display visually:

🟢 Available

🔴 Booked

🟡 Maintenance

⚫ Unavailable

🟠 Pending

Status must be calculated from actual backend data.

---

# 14. RESOURCE DETAILS PAGE

Create:

```text
/resources/:id
```

Show:

```text
ROOM 204

Block A
2nd Floor

Classroom

Capacity: 60

Facilities:
✓ Projector
✓ Smart Board
✓ AC
✓ Wi-Fi

Status:
AVAILABLE

[CHECK AVAILABILITY]

[BOOK NOW]
```

Include:

* Images
* Location
* Floor
* Capacity
* Amenities
* Booking history summary
* Availability timeline

---

# 15. REAL-TIME AVAILABILITY

Create a proper availability engine.

Given:

```text
Resource
Date
Start time
End time
```

return:

```text
AVAILABLE
```

or:

```text
UNAVAILABLE
```

Reasons:

* Existing booking
* Maintenance
* Block
* Outside working hours
* Resource unavailable

Never fake availability.

---

# 16. AVAILABILITY TIMELINE

Display:

```text
ROOM 204

08:00 ─ AVAILABLE
09:00 ─ AVAILABLE
10:00 ─ BOOKED
11:00 ─ BOOKED
12:00 ─ AVAILABLE
01:00 ─ AVAILABLE
02:00 ─ BOOKED
03:00 ─ BOOKED
04:00 ─ AVAILABLE
```

Allow selecting an available time slot.

---

# 17. SMART RESOURCE SEARCH

Create global search.

Users can type:

```text
Find a classroom for 60 students.
```

or:

```text
Find a lab with 30 computers.
```

or:

```text
Find a seminar hall tomorrow from 2 PM to 4 PM.
```

Filters:

* Building
* Floor
* Type
* Capacity
* Facilities
* Department
* Date
* Time
* Status

---

# 18. AI BOOKING ASSISTANT

Create:

```text
/ai-booking
```

Design this as a premium AI interface.

Example:

```text
┌─────────────────────────────────────┐
│ AI CAMPUS BOOKING ASSISTANT         │
│                                     │
│ "Find me a classroom for 50        │
│ students tomorrow from 2–4 PM      │
│ with a projector."                 │
│                                     │
│              [Find Resources]       │
└─────────────────────────────────────┘
```

---

# 19. GROQ AI FLOW

Flow:

```text
User Prompt
 ↓
React
 ↓
Spring Boot
 ↓
Groq
 ↓
Structured Intent
 ↓
Backend Validation
 ↓
SQL Availability
 ↓
Recommendation
 ↓
User Confirmation
```

Groq should extract:

```json
{
  "resourceType": "CLASSROOM",
  "capacity": 50,
  "date": "2026-08-28",
  "startTime": "14:00",
  "endTime": "16:00",
  "requiredFacilities": ["PROJECTOR"],
  "building": null
}
```

Do not trust AI output blindly.

Validate everything using backend DTOs and business rules.

---

# 20. AI MUST NEVER DIRECTLY BOOK

Groq cannot:

* create bookings
* approve bookings
* modify database
* delete data
* change permissions

AI only interprets the user's request and assists with recommendations.

Spring Boot remains the authority.

---

# 21. AI HALLUCINATION PROTECTION

If AI suggests:

```text
Room 999
```

but Room 999 does not exist:

Do NOT display it as real.

Validate every resource against SQL.

Return:

```text
That resource does not exist.

Here are the available alternatives:
...
```

---

# 22. AI CHAT ASSISTANT

Create a general campus assistant.

Users can ask:

> What rooms are available now?

> Where is the Physics Lab?

> Which block has the largest seminar hall?

> Show my upcoming bookings.

> Find a room for 100 students.

> What resources are available in Block A?

The assistant should retrieve real database information through controlled backend tools/services.

Never allow the LLM to invent database information.

---

# 23. AI RECOMMENDATION ENGINE

Use deterministic backend scoring.

Score based on:

1. Availability
2. Capacity fit
3. Facilities
4. Location
5. Resource type
6. Utilization balancing

Groq may generate a human-readable explanation.

Example:

> Room 204 is recommended because it exactly fits your group of 50, has a projector, and is available for the requested time.

---

# 24. BOOKING WIZARD

Create:

```text
STEP 1
Select Resource

STEP 2
Date & Time

STEP 3
Purpose

STEP 4
Participants

STEP 5
Requirements

STEP 6
Review

STEP 7
Confirm
```

Show availability during the process.

---

# 25. VISUAL BOOKING EXPERIENCE

After selecting a room, show:

```text
ROOM 204

Floor Map:

[201] [202] [203]
      ↓
    [204] ← YOU ARE HERE
      ↓
 [LAB] [LAB]

Available Times:

10:00 ─────────
11:00 █ BOOKED
12:00 ─────────
01:00 ─────────
02:00 █ BOOKED
03:00 ─────────
```

The booking experience should feel visual rather than form-heavy.

---

# 26. MULTI-RESOURCE BOOKING

Allow one booking to reserve multiple resources.

Example:

```text
TECH EVENT

Seminar Hall
+
Projector
+
Microphones
+
Lab
```

Check conflicts for every resource.

Use transactions.

---

# 27. EVENT BOOKING

Create an event booking feature.

Fields:

* Event name
* Organizer
* Date
* Start/end time
* Expected attendees
* Required rooms
* Equipment
* Description

Show the complete event resource allocation visually.

---

# 28. EXAMINATION MODE

Admin can create an examination.

Example:

```text
EXAMINATION

Date:
20 September

Time:
10 AM – 1 PM

Required Capacity:
500
```

System automatically finds:

```text
Block A
Rooms 101–110

Block B
Rooms 201–210

Block C
Rooms 301–305
```

Detect conflicts.

---

# 29. RECURRING BOOKINGS

Support:

* Daily
* Weekly
* Monthly
* Custom recurrence

Example:

```text
Every Monday
10 AM – 12 PM
For 3 months
```

Check every occurrence.

---

# 30. CONFLICT DETECTION

Detect:

* Same resource overlap
* User double booking
* Maintenance
* Block
* Outside working hours
* Capacity
* Invalid duration

Return meaningful errors.

---

# 31. RACE CONDITION PROTECTION

Two users must not be able to book the same room simultaneously.

Use:

* Transactions
* Proper isolation
* Database constraints where appropriate
* Backend revalidation

The booking process must be atomic.

---

# 32. APPROVAL SYSTEM

Roles:

## STUDENT

* Search
* View
* Book
* Cancel own bookings
* Check-in
* Check-out

## PROFESSOR

* Student capabilities
* Review assigned booking requests
* Approve/reject assigned requests
* Create bookings

## ADMIN

* Full control

Workflow:

```text
Student
 ↓
Professor Approval
 ↓
Admin Approval
 ↓
Confirmed
```

---

# 33. APPROVAL DASHBOARD

Professor:

```text
PENDING APPROVALS

Room 204
Tomorrow
2–4 PM

Purpose:
Project Discussion

[APPROVE] [REJECT]
```

Admin:

```text
ADMIN APPROVALS

Professor Approved
Waiting for Admin
```

---

# 34. QR RESOURCE SYSTEM

Generate secure QR codes.

Each resource has:

```text
RESOURCE QR
```

Scan:

```text
QR
 ↓
Resource Page
 ↓
Availability
 ↓
Book
```

---

# 35. QR CHECK-IN

Flow:

```text
Scan QR
 ↓
Authenticate
 ↓
Find user's booking
 ↓
Validate time
 ↓
Check resource
 ↓
CHECKED IN
```

Users cannot check into another user's booking.

---

# 36. CHECK-OUT

After using the resource:

```text
CHECK OUT
```

Store:

* Check-in
* Check-out
* Duration

Use actual usage for analytics.

---

# 37. NO-SHOW

Example:

```text
Booking:
2:00 PM

Grace period:
15 minutes

No check-in:
2:15 PM

→ NO SHOW
→ RELEASE RESOURCE
```

Track no-shows per user.

---

# 38. WAITLIST

If a resource is fully booked:

```text
ROOM 204
Fully Booked

[JOIN WAITLIST]
```

If cancelled:

```text
Waitlist
 ↓
Next eligible user
 ↓
Notification
 ↓
Reservation window
```

---

# 39. FAVORITES

Allow users to favorite:

* Rooms
* Labs
* Seminar halls

Show:

```text
MY FAVORITES

⭐ Room 204
⭐ Computer Lab 2
⭐ Seminar Hall
```

---

# 40. QUICK REBOOK

After completed booking:

```text
BOOK AGAIN
```

Automatically reuse:

* Resource
* Duration
* Purpose
* Requirements

Only date/time needs to change.

---

# 41. WHAT'S AVAILABLE NOW

Create a special feature:

```text
WHAT'S AVAILABLE NOW?
```

Display:

```text
BLOCK A

Room 101 🟢
Room 102 🔴
Room 103 🟢

LAB 201 🟢

SEMINAR HALL 🔴
```

Filter by building/floor/resource type.

---

# 42. LIVE CAMPUS OCCUPANCY

Dashboard:

```text
CAMPUS LIVE STATUS

Available        48
Occupied         31
Maintenance       6
Blocked           3

Current Occupancy
42%
```

Show building-level occupancy.

---

# 43. CAMPUS HEATMAP

Create visual utilization heatmap.

Example:

```text
BLOCK A       🔴 82%
BLOCK B       🟡 54%
BLOCK C       🟢 31%
CENTRAL       🔴 76%
```

Allow date range.

---

# 44. RESOURCE UTILIZATION

Formula:

```text
Booked Hours
────────────── × 100
Available Hours
```

Display:

* Resource utilization
* Building utilization
* Floor utilization
* Department utilization

Use Recharts.

---

# 45. UNDERUTILIZATION AI

Detect:

```text
Room 301
Capacity: 100
Average attendance: 18
Utilization: 21%
```

Recommendation:

> Consider assigning smaller rooms for similar events and reserving Room 301 for large groups.

---

# 46. CAPACITY OPTIMIZATION

Compare expected participants against capacity.

Example:

```text
Expected: 20
Room capacity: 100
```

Recommend:

```text
Room 204
Capacity: 25
```

This improves campus resource efficiency.

---

# 47. RESOURCE MAINTENANCE

Admin can create:

```text
Room 204
Maintenance

10 Sep
-
12 Sep
```

During this period:

* Prevent booking
* Display maintenance
* Show reason

---

# 48. ISSUE REPORTING

Users can report:

* Broken projector
* AC problem
* Computer problem
* Wi-Fi problem
* Damaged furniture
* Other issue

Workflow:

```text
Reported
 ↓
Assigned
 ↓
In Progress
 ↓
Resolved
```

Track resolution time.

---

# 49. EQUIPMENT BOOKING

Allow booking:

* Projector
* Laptop
* Camera
* Microphone
* Speaker
* Other equipment

Equipment can be attached to a room booking.

---

# 50. NOTIFICATION SYSTEM

In-app notifications:

* Booking submitted
* Professor approval
* Admin approval
* Booking confirmed
* Booking rejected
* Booking cancelled
* Upcoming booking
* Check-in reminder
* Waitlist available
* Maintenance notice

---

# 51. USER DASHBOARD

## STUDENT

Display:

```text
Welcome back 👋

Upcoming Booking
Room 204
Today • 2–4 PM

Pending:
2

Confirmed:
5

Completed:
18
```

---

# 52. PROFESSOR DASHBOARD

Display:

```text
Pending Approvals
8

Today's Bookings
5

Upcoming
12

Approval History
```

---

# 53. ADMIN DASHBOARD

Display:

```text
Total Buildings       8
Total Floors          32
Total Resources      164
Available Now         72
Bookings Today        48
Pending Approvals      9
Occupancy             58%
```

Charts:

* Booking trends
* Building usage
* Resource utilization
* Peak hours
* Booking types
* Cancellation rate
* No-show rate

All values must come from SQL.

---

# 54. ADMIN CAMPUS MANAGEMENT

Admin can manage:

```text
Buildings
Floors
Resources
Facilities
Equipment
Bookings
Users
Maintenance
Issues
Reports
Audit Logs
```

---

# 55. RESOURCE MANAGEMENT

Admin can:

* Create
* Edit
* Disable
* Delete only where safe
* Block
* Maintenance
* Change capacity
* Add facilities
* Configure working hours
* Configure floor position

---

# 56. DATABASE DESIGN

Create entities:

```text
User
Role
Building
Floor
Resource
ResourceType
Facility
ResourceFacility
Booking
BookingResource
BookingApproval
Maintenance
ResourceBlock
CheckIn
Waitlist
Favorite
Notification
Issue
Equipment
Event
AuditLog
```

Relationships:

```text
Building
 ↓
Floor
 ↓
Resource
 ↓
Booking
```

Use proper foreign keys.

---

# 57. AUTHENTICATION

Implement Spring Security + JWT.

Features:

* Register
* Login
* Logout
* Password hashing
* JWT
* Role authorization
* Protected routes
* Token expiration
* Refresh token if appropriate

---

# 58. BACKEND API

Create clean REST APIs.

Examples:

```text
POST /api/auth/register
POST /api/auth/login

GET /api/campus

GET /api/buildings
GET /api/buildings/{id}

GET /api/buildings/{id}/floors

GET /api/floors/{id}
GET /api/floors/{id}/resources

GET /api/resources
GET /api/resources/{id}

GET /api/resources/{id}/availability

POST /api/bookings
GET /api/bookings/my
GET /api/bookings/{id}

PUT /api/bookings/{id}
POST /api/bookings/{id}/cancel

POST /api/bookings/{id}/approve
POST /api/bookings/{id}/reject

POST /api/bookings/{id}/check-in
POST /api/bookings/{id}/check-out

GET /api/dashboard

POST /api/ai/interpret
POST /api/ai/recommend

GET /api/notifications
```

Use:

* DTOs
* Services
* Repositories
* Controllers
* Validation
* Global exception handler

---

# 59. API ERROR FORMAT

Use consistent responses.

Example:

```json
{
  "timestamp": "2026-08-27T10:30:00",
  "status": 409,
  "error": "BOOKING_CONFLICT",
  "message": "Room 204 is already booked from 2 PM to 4 PM."
}
```

---

# 60. DATABASE INDEXING

Add indexes for frequently searched fields:

* building_id
* floor_id
* resource_type
* booking_date
* start_time
* end_time
* status

Optimize queries.

---

# 61. AUDIT LOG

Record:

```text
WHO
WHAT
WHEN
```

Examples:

```text
Student created booking
Professor approved booking
Admin rejected booking
Admin modified resource
Student checked in
Student checked out
```

---

# 62. GLOBAL SEARCH

Create a global search bar in the navigation.

Search:

```text
Room 204
Physics Lab
Seminar Hall
Central Block
Library
Projector
```

Results should link directly to the appropriate resource.

---

# 63. BREADCRUMBS

Use:

```text
Campus
 >
Block A
 >
2nd Floor
 >
Room 204
```

This improves navigation.

---

# 64. RESOURCE FAVORITES

Allow users to favorite frequently used resources.

---

# 65. RECENTLY VISITED

Show:

```text
Recently Viewed

Room 204
Block A
Computer Lab
Seminar Hall
```

---

# 66. DARK/LIGHT MODE

Provide:

* Light mode
* Dark mode
* System mode

Ensure campus maps and status colors remain readable.

---

# 67. RESPONSIVE DESIGN

The entire system must work on:

* Desktop
* Laptop
* Tablet
* Mobile

Mobile should prioritize:

* Search
* Booking
* QR scanning
* Check-in
* Notifications

---

# 68. ACCESSIBILITY

Implement:

* Keyboard navigation
* Accessible buttons
* Labels
* Good contrast
* Focus states
* ARIA where needed
* Screen-reader-friendly structure

---

# 69. PREMIUM UI DESIGN

The UI should feel like a **high-end SaaS product combined with a modern university portal**.

Use:

* Glass effects where appropriate
* Subtle gradients
* Clean cards
* Smooth animations
* Modern typography
* Spacious layout
* Interactive maps
* Beautiful dashboards
* Status indicators
* Skeleton loaders
* Toast notifications

Do not overuse animations.

Prioritize usability.

---

# 70. LANDING PAGE

Create:

```text
College Resource Booking Portal
```

Hero:

> Explore your campus. Find the right resource. Book it in seconds.

Buttons:

```text
Explore Campus
Book a Resource
AI Assistant
```

Show visual campus representation.

---

# 71. QUICK BOOKING

From dashboard:

```text
QUICK BOOK

Resource Type
Date
Start
End
Capacity

[Find Available Resources]
```

Display results immediately.

---

# 72. VISUAL BOOKING CONFIRMATION

After booking:

Show an attractive confirmation page.

```text
✓ BOOKING REQUESTED

Room 204
Block A
2nd Floor

Tomorrow
2:00 PM – 4:00 PM

Status:
Pending Professor Approval

[View Booking]
[Add to Calendar]
```

---

# 73. BOOKING DETAILS

Display timeline:

```text
REQUESTED
   ↓
PROFESSOR APPROVED
   ↓
ADMIN APPROVED
   ↓
CONFIRMED
   ↓
CHECKED IN
   ↓
COMPLETED
```

This should be visually represented.

---

# 74. VISUAL APPROVAL TIMELINE

Use:

```text
● Requested
│
● Professor Approved
│
● Admin Approved
│
● Confirmed
```

For rejected:

```text
● Requested
│
● Rejected
```

Show reason.

---

# 75. CALENDAR INTEGRATION

Provide optional:

* Download calendar event
* `.ics` generation

Google Calendar/Outlook integrations can remain optional.

Do not make external services mandatory.

---

# 76. REPORTING

Create reports:

* Daily bookings
* Weekly bookings
* Monthly bookings
* Resource utilization
* Building utilization
* Peak hours
* Cancellation
* No-show
* Maintenance
* Issue resolution

Allow:

* CSV
* Excel
* PDF

---

# 77. AI ANALYTICS

Use Groq to generate natural-language explanations from backend-generated statistics.

Example:

Backend calculates:

```text
Room 204 utilization = 87%
Room 301 utilization = 22%
```

AI can explain:

> Room 204 is heavily utilized while Room 301 remains significantly underused.

The AI must NOT invent statistics.

---

# 78. AI CAMPUS INSIGHTS

Admin can ask:

> Which building is most utilized?

> Which rooms are underused?

> What are peak booking hours?

> Which resources should be allocated differently?

Backend retrieves real metrics first.

Groq summarizes them.

---

# 79. SECURITY RULE

AI must never be allowed to:

* Modify database directly
* Approve bookings
* Delete resources
* Change roles
* Bypass permissions

All actions must go through normal Spring Boot authorization/business logic.

---

# 80. DEMO DATA

Create realistic seed data based on the provided campus image.

For normal blocks:

* Multiple floors
* 5+ rooms per floor
* 2+ labs per floor
* 1 seminar hall

Central Block:

* 1st Floor → Seminar Hall
* 2nd Floor → Library
* 3rd Floor → Library

Create:

* Students
* Professors
* Admins
* Resources
* Bookings
* Maintenance
* Sample notifications
* Sample analytics

---

# 81. DEMO ACCOUNTS

Create development-only demo accounts:

```text
student@example.com
professor@example.com
admin@example.com
```

Use environment/configuration for passwords.

Do not expose real credentials in production.

---

# 82. HACKATHON DEMO STORY

The application must support this complete story:

```text
LOGIN
 ↓
EXPLORE CAMPUS
 ↓
SELECT BUILDING
 ↓
SELECT FLOOR
 ↓
VISUAL FLOOR MAP
 ↓
SELECT ROOM
 ↓
CHECK LIVE AVAILABILITY
 ↓
BOOK RESOURCE
 ↓
PROFESSOR APPROVAL
 ↓
ADMIN APPROVAL
 ↓
CONFIRMED
 ↓
QR CODE
 ↓
CHECK-IN
 ↓
USE RESOURCE
 ↓
CHECK-OUT
 ↓
ANALYTICS UPDATE
```

Then demonstrate AI:

```text
User:

"I need a seminar hall for 100 people
tomorrow from 10 AM to 1 PM
with a projector."

 ↓

Groq interprets request

 ↓

Spring Boot validates

 ↓

SQL availability search

 ↓

Best halls displayed

 ↓

User selects

 ↓

Booking workflow
```

---

# 83. AI FAILURE HANDLING

If Groq fails:

The normal booking system must continue working.

Display:

> AI assistant is temporarily unavailable. You can continue with manual booking.

Never make AI a single point of failure.

---

# 84. ENVIRONMENT VARIABLES

Create:

```text
backend/.env
```

with:

```text
GROQ_API_KEY=your_key_here

DB_URL=jdbc:mysql://localhost:3306/college_resource_booking
DB_USERNAME=root
DB_PASSWORD=your_password

JWT_SECRET=your_secret
```

Create:

```text
.env.example
```

Never commit `.env`.

---

# 85. BACKEND PACKAGE STRUCTURE

Use a clean structure such as:

```text
backend/
└── src/main/java/com/college/booking/

    controller/
    service/
    repository/
    entity/
    dto/
    security/
    exception/
    config/
    mapper/
    util/
```

Keep the architecture clean.

---

# 86. FRONTEND STRUCTURE

Use:

```text
frontend/
└── src/

    components/
    pages/
    layouts/
    hooks/
    services/
    api/
    context/
    utils/
    routes/
    assets/
```

Create reusable components.

---

# 87. FRONTEND STATE

Use appropriate React state management.

Avoid unnecessary global state.

Centralize API calls.

Handle:

* Loading
* Error
* Success
* Empty state

---

# 88. TESTING

Implement backend tests for:

* Authentication
* Authorization
* Resource CRUD
* Booking
* Conflict detection
* Approval
* Cancellation
* Check-in
* Check-out
* Waitlist
* Maintenance

Test frontend critical flows:

* Login
* Campus navigation
* Booking
* Approval
* Cancellation
* QR flow

---

# 89. FINAL QUALITY CHECK

Before considering the project complete:

### Backend

* Spring Boot starts successfully
* Database connects
* JPA works
* JWT works
* APIs work
* Groq integration works
* No compilation errors

### Frontend

* Vite starts
* React builds
* No console errors
* API integration works
* Responsive design works

### Database

* Tables created
* Relationships work
* Seed data works
* Queries work

### Booking

* Availability works
* Conflict detection works
* Approval works
* Cancellation works
* Check-in works
* Check-out works

### AI

* Groq works
* Invalid AI output is rejected
* AI never bypasses backend rules
* Manual booking works if Groq fails

---

# 90. DO NOT USE

Absolutely do NOT use:

* FastAPI
* Python backend
* Django
* PostgreSQL
* MongoDB
* TypeScript
* Firebase as the main database
* Hardcoded availability
* Fake statistics
* Fake AI responses
* Fake booking confirmation
* Frontend-only authorization
* Groq directly from React
* AI-generated resources that don't exist in SQL

---

# 91. FINAL PRODUCT

The final system should look and behave like:

## "DIGITAL CAMPUS RESOURCE OPERATING SYSTEM"

It should combine:

### 🗺️ Campus Digital Twin

Visual campus → buildings → floors → resources

### 📅 Smart Booking

Availability → calendar → booking → approval

### 🤖 AI Assistant

Natural language → intent → database search → recommendations

### 📱 QR System

Resource QR → booking → check-in

### 📊 Analytics

Usage → occupancy → utilization → optimization

### 🛠️ Resource Management

Maintenance → issues → blocking → availability

### 🎓 Academic Operations

Classrooms → labs → seminar halls → examinations → events

---

# 92. SIGNATURE USER EXPERIENCE

The most important experience should be:

```text
             COLLEGE CAMPUS
                    ↓
             SELECT BUILDING
                    ↓
               SELECT FLOOR
                    ↓
          VISUAL FLOOR MAP
                    ↓
          SELECT RESOURCE
                    ↓
       SEE LIVE AVAILABILITY
                    ↓
             BOOK RESOURCE
                    ↓
              APPROVAL
                    ↓
             CONFIRMATION
                    ↓
               QR CHECK-IN
                    ↓
                CHECK-OUT
                    ↓
              ANALYTICS
```

And the AI experience:

```text
"Find me a lab for 40 students
tomorrow from 10 AM to 12 PM
with computers."

              ↓

             GROQ AI

              ↓

       STRUCTURED INTENT

              ↓

       SPRING BOOT VALIDATION

              ↓

           SQL SEARCH

              ↓

       AVAILABLE LABS

              ↓

        SMART RECOMMENDATION

              ↓

             BOOK
```

---

# 93. MOST IMPORTANT PRINCIPLE

The application must be **database-driven, secure, visually impressive and genuinely functional**.

The system should never pretend that something exists when it does not.

The database is the source of truth.

Spring Boot is the source of business logic.

React is the source of user experience.

Groq is the AI assistant.

The college campus structure is the visual foundation.

---

# 94. START NOW

Start by:

1. Creating the project structure.
2. Creating the Spring Boot backend.
3. Creating the React frontend.
4. Connecting MySQL.
5. Designing the database.
6. Implementing authentication.
7. Implementing campus/building/floor/resource entities.
8. Creating the campus visual interface.
9. Creating floor maps.
10. Creating the booking engine.
11. Creating availability.
12. Creating approval workflow.
13. Creating QR.
14. Creating check-in/check-out.
15. Creating AI integration with Groq.
16. Creating dashboards.
17. Creating analytics.
18. Creating maintenance.
19. Creating waitlist.
20. Creating examination/event booking.
21. Creating admin management.
22. Creating reports.
23. Adding animations and UI polish.
24. Seeding realistic data.
25. Testing the complete application.

Do not stop after generating files.

Run the application.

Fix errors.

Verify the APIs.

Verify the database.

Verify the frontend.

Verify the complete booking workflow.

The final result must be a **fully runnable, polished, hackathon-ready College Resource Booking Platform** built with:

**React + JavaScript + Vite + Spring Boot + Java + Spring Security + JWT + JPA/Hibernate + Local MySQL + Groq AI**

with the provided college image serving as the reference for the **interactive campus/building/floor/resource visualization**.
