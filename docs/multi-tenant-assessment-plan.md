# Multi-Tenant Assessment Plan

This document tracks the next product level for Code Training Lab: moving from a solo learning platform to a multi-tenant assessment platform where platform super users manage organizations, organization admins manage assessments, and candidates complete invite-only tests.

The existing MVP spec intentionally says "no organizations". This plan is a future direction and should be treated as a new product track, not a correction to the current MVP.

## Product Goal

Build a real-world hiring, training, and evaluation layer on top of the current challenge runner.

The first complete slice should be:

1. A super admin creates an organization.
2. The super admin creates or invites one or more organization admins.
3. An organization admin creates an assessment from existing code challenges.
4. The organization admin assigns the assessment to candidates by email.
5. A candidate opens an invite link and completes the assessment.
6. The organization admin reviews results, code, questions, scores, and integrity signals.

## Core Product Decisions

| Decision | Recommendation | Reason |
| --- | --- | --- |
| Organization users | Organizations have admin users only | Matches the requested model and keeps tenant membership simple |
| Candidates | Model candidates separately from organization users | Candidates should not become org members just because they take a test |
| Super user | Add `SUPER_ADMIN` role | Current `ADMIN` role is too broad for multi-tenant operations |
| Organization admin | Add `ORG_ADMIN` role | Org admins can manage only their organizations |
| Challenge customization | Use copy-on-write variants | Admin customizations should not mutate global challenge definitions |
| Assessment publishing | Publish immutable versions | Candidate reports must remain stable even if admins edit a draft later |
| Candidate access | Secure invite token, not password account in v1 | Reduces candidate friction and avoids account lifecycle complexity |
| Profile photos/logos | Start with URL or stored object metadata | Easier to support self-hosting and later object storage |

## Roles And Permissions

### `SUPER_ADMIN`

Platform-level operator.

Can:

- Create, update, suspend, and reactivate organizations.
- Create and deactivate organization admins.
- View all organizations and high-level platform health.
- Manage global challenge catalog and challenge collections.
- Access runner/LSP operations.
- View audit events across all organizations.

Cannot:

- Silently change a published assessment version. Published versions should be immutable.

### `ORG_ADMIN`

Organization-level assessment manager.

Can:

- Edit the organization profile.
- Manage organization admin profiles, depending on future owner permissions.
- Create assessment drafts.
- Add coding challenges and question challenges to assessments.
- Customize challenge instructions, tests, timers, score weights, and rubric fields through variants.
- Publish assessment versions.
- Assign assessments to candidates by email.
- View candidate attempts, results, code, answers, integrity events, and exports for their organization.

Cannot:

- See another organization's data.
- Manage platform operations.
- Change global catalog challenges unless explicitly allowed later.

### Candidate

Invite-only participant.

Can:

- Open an assignment through a tokenized email link.
- Review instructions.
- Complete coding challenges and question challenges.
- Submit final answers.
- Optionally view completion confirmation or limited results, depending on assessment settings.

Cannot:

- Browse the public challenge catalog during an assessment.
- See hidden tests.
- See other candidates or organization admin pages.
- Use the invite after expiration, revocation, or final submission.

## Domain Model

### New Tables

#### `organizations`

Stores organization profile and lifecycle.

Suggested fields:

- `id`
- `name`
- `slug`
- `description`
- `website_url`
- `industry`
- `company_size`
- `country`
- `timezone`
- `contact_email`
- `logo_url`
- `status`: `ACTIVE`, `SUSPENDED`, `ARCHIVED`
- `created_at`
- `updated_at`
- `deleted_at`

#### `organization_members`

Connects platform users to organizations.

Suggested fields:

- `id`
- `organization_id`
- `user_id`
- `role`: initially `ORG_ADMIN`
- `created_at`
- `updated_at`
- unique `(organization_id, user_id)`

#### `user_profiles`

Either a separate table or extra columns on `users`. A separate table is cleaner once profile data grows.

Suggested fields:

- `user_id`
- `full_name`
- `title`
- `phone`
- `timezone`
- `profile_photo_url`
- `bio`
- `updated_at`

Current `users.full_name` can be migrated or left as the canonical display name while profile data expands.

#### `challenge_collections`

Groups current code challenges.

Suggested fields:

- `id`
- `organization_id`, nullable for platform/global collections
- `name`
- `description`
- `visibility`: `GLOBAL`, `ORGANIZATION`
- `created_by_user_id`
- `created_at`
- `updated_at`

#### `challenge_collection_items`

Suggested fields:

- `id`
- `collection_id`
- `challenge_id`
- `sort_order`
- unique `(collection_id, challenge_id)`

#### `challenge_variants`

Organization-specific or assessment-specific customization of a base challenge.

Suggested fields:

- `id`
- `organization_id`
- `base_challenge_id`
- `title`
- `description_md`
- `starter_code`
- `gating_config`
- `session_duration_minutes`
- `created_by_user_id`
- `created_at`
- `updated_at`

Hidden/public tests can either be copied into variant-specific test tables or represented as override JSON. Prefer normalized tables if admins will edit tests in the UI.

#### `question_challenges`

Stores non-code challenge content.

Suggested fields:

- `id`
- `organization_id`, nullable for global question bank
- `type`: `MULTIPLE_CHOICE`, `MULTI_SELECT`, `SHORT_ANSWER`, `ESSAY`, `SQL`, `SYSTEM_DESIGN`
- `title`
- `prompt_md`
- `options_json`
- `correct_answer_json`
- `rubric_json`
- `default_points`
- `created_by_user_id`
- `created_at`
- `updated_at`
- `deleted_at`

#### `assessments`

Draft/editable assessment container.

Suggested fields:

- `id`
- `organization_id`
- `name`
- `description_md`
- `instructions_md`
- `status`: `DRAFT`, `PUBLISHED`, `ARCHIVED`
- `time_limit_minutes`
- `passing_score`
- `shuffle_items`
- `show_results_to_candidate`
- `created_by_user_id`
- `created_at`
- `updated_at`

#### `assessment_items`

Items inside the editable draft.

Suggested fields:

- `id`
- `assessment_id`
- `item_type`: `CODE_CHALLENGE`, `QUESTION_CHALLENGE`
- `challenge_id`, nullable
- `challenge_variant_id`, nullable
- `question_challenge_id`, nullable
- `title_override`
- `points`
- `required`
- `sort_order`
- `time_limit_minutes`

#### `assessment_versions`

Immutable published snapshot.

Suggested fields:

- `id`
- `assessment_id`
- `version_number`
- `snapshot_json`
- `published_by_user_id`
- `published_at`

The snapshot should include enough item details to render and score the assessment even if source challenges are later edited.

#### `candidates`

Candidate directory scoped to an organization.

Suggested fields:

- `id`
- `organization_id`
- `email`
- `full_name`
- `phone`
- `linkedin_url`
- `github_url`
- `location`
- `created_at`
- `updated_at`
- unique `(organization_id, lower(email))`

#### `assessment_assignments`

One candidate assignment to one published assessment version.

Suggested fields:

- `id`
- `organization_id`
- `candidate_id`
- `assessment_id`
- `assessment_version_id`
- `status`: `INVITED`, `OPENED`, `STARTED`, `SUBMITTED`, `EXPIRED`, `CANCELLED`
- `invite_token_hash`
- `invite_sent_at`
- `expires_at`
- `due_at`
- `created_by_user_id`
- `created_at`
- `updated_at`

Store only a token hash. Never store raw invite tokens.

#### `assessment_attempts`

Candidate's active or completed attempt.

Suggested fields:

- `id`
- `assignment_id`
- `started_at`
- `submitted_at`
- `score`
- `max_score`
- `passed`
- `integrity_summary_json`
- `created_at`
- `updated_at`

#### `assessment_item_attempts`

Per-item state and results.

Suggested fields:

- `id`
- `attempt_id`
- `assessment_item_snapshot_id` or stable item key from `snapshot_json`
- `submission_id`, nullable for coding items
- `answer_json`, nullable for question items
- `score`
- `max_score`
- `status`: `NOT_STARTED`, `IN_PROGRESS`, `SUBMITTED`, `SCORED`
- `manual_review_required`
- `review_notes`
- `reviewed_by_user_id`
- `reviewed_at`

#### `audit_events`

Tenant-aware audit trail.

Suggested fields:

- `id`
- `organization_id`, nullable for platform events
- `actor_user_id`, nullable for candidate token actions
- `candidate_id`, nullable
- `event_type`
- `entity_type`
- `entity_id`
- `metadata_json`
- `created_at`

## Changes To Existing Tables

### `users`

Add:

- `role` values: `SUPER_ADMIN`, `ORG_ADMIN`, optionally keep `ADMIN` during migration.
- `profile_photo_url`, if not using `user_profiles`.
- `last_login_at`, useful for admin management.

Migration idea:

1. Add new role values.
2. Convert existing `ADMIN` users to `SUPER_ADMIN`.
3. Convert existing `USER` learners based on product decision:
   - Keep as learner users if solo learning remains.
   - Or leave untouched and add candidate flow separately.

### `submissions`

Add nullable assessment context:

- `assessment_attempt_id`
- `assessment_item_attempt_id`
- `organization_id`

This lets the existing runner pipeline keep working while reports can distinguish practice submissions from assessment submissions.

### `custom_tests`

Current custom tests are per user and challenge. For assessment mode, custom tests should be either:

- disabled for candidates, or
- stored per assessment attempt item and excluded from scoring.

Recommended v1: candidates cannot customize tests in scored assessment mode.

## Backend Package Plan

Follow the existing modular monolith style from `be/ARCHITECTURE.md`.

Suggested packages:

```text
organization/
  api/
  application/

assessment/
  api/
  application/
  messaging/        # optional later for reminder emails and async scoring

question/
  api/
  application/
```

Keep shared persistence entities in `platform.persistence` if the repo continues that convention. Keep feature rules in the feature `application` packages.

## API Plan

### Super Admin APIs

```text
GET    /api/v1/super/organizations
POST   /api/v1/super/organizations
GET    /api/v1/super/organizations/{organizationId}
PATCH  /api/v1/super/organizations/{organizationId}
POST   /api/v1/super/organizations/{organizationId}/suspend
POST   /api/v1/super/organizations/{organizationId}/reactivate

GET    /api/v1/super/organizations/{organizationId}/admins
POST   /api/v1/super/organizations/{organizationId}/admins
DELETE /api/v1/super/organizations/{organizationId}/admins/{userId}
```

### Organization Admin APIs

```text
GET    /api/v1/orgs/{organizationId}/profile
PATCH  /api/v1/orgs/{organizationId}/profile

GET    /api/v1/orgs/{organizationId}/assessments
POST   /api/v1/orgs/{organizationId}/assessments
GET    /api/v1/orgs/{organizationId}/assessments/{assessmentId}
PATCH  /api/v1/orgs/{organizationId}/assessments/{assessmentId}
POST   /api/v1/orgs/{organizationId}/assessments/{assessmentId}/items
PATCH  /api/v1/orgs/{organizationId}/assessments/{assessmentId}/items/{itemId}
DELETE /api/v1/orgs/{organizationId}/assessments/{assessmentId}/items/{itemId}
POST   /api/v1/orgs/{organizationId}/assessments/{assessmentId}/publish

GET    /api/v1/orgs/{organizationId}/candidates
POST   /api/v1/orgs/{organizationId}/candidates
POST   /api/v1/orgs/{organizationId}/candidates/import

GET    /api/v1/orgs/{organizationId}/assignments
POST   /api/v1/orgs/{organizationId}/assignments
POST   /api/v1/orgs/{organizationId}/assignments/{assignmentId}/resend
POST   /api/v1/orgs/{organizationId}/assignments/{assignmentId}/cancel

GET    /api/v1/orgs/{organizationId}/attempts/{attemptId}/report
PATCH  /api/v1/orgs/{organizationId}/attempts/{attemptId}/items/{itemAttemptId}/score
```

### Candidate APIs

```text
GET    /api/v1/invitations/{token}
POST   /api/v1/invitations/{token}/start
GET    /api/v1/attempts/{attemptId}
POST   /api/v1/attempts/{attemptId}/items/{itemId}/run
POST   /api/v1/attempts/{attemptId}/items/{itemId}/submit
POST   /api/v1/attempts/{attemptId}/submit
```

Candidate endpoints should authenticate with a scoped attempt token or short-lived candidate JWT minted from the invite token.

## Frontend Route Plan

### Super Admin

```text
/super/organizations
/super/organizations/new
/super/organizations/:organizationId
/super/organizations/:organizationId/admins
/super/catalog
/super/audit
```

### Organization Admin

```text
/org/:organizationId/dashboard
/org/:organizationId/profile
/org/:organizationId/admins
/org/:organizationId/challenge-collections
/org/:organizationId/assessments
/org/:organizationId/assessments/new
/org/:organizationId/assessments/:assessmentId/builder
/org/:organizationId/assignments
/org/:organizationId/candidates
/org/:organizationId/reports
/org/:organizationId/reports/:attemptId
```

### Candidate

```text
/invite/:token
/assessment/:attemptId
/assessment/:attemptId/complete
```

## UI Surfaces

### Organization Management

Super admin organization table:

- Name and logo
- Status
- Admin count
- Open assignments
- Completed attempts
- Last activity
- Actions: view, suspend, reactivate

Organization profile form:

- Logo/profile photo
- Name
- Website
- Description
- Industry
- Size
- Country/timezone
- Contact email

### Admin Profile

Basic admin profile:

- Profile photo
- Full name
- Email
- Title
- Phone
- Timezone
- Password change flow

### Assessment Builder

Recommended layout:

- Left panel: ordered assessment items and sections.
- Main panel: selected coding challenge or question editor.
- Right panel: points, timer, required flag, scoring, integrity settings.
- Top actions: preview, validate, publish, assign candidates.

Builder features:

- Add existing code challenge.
- Add challenge collection.
- Add question challenge.
- Customize item title and instructions.
- Override time limit and points.
- Preview candidate experience.
- Publish immutable version.

### Candidate Assessment

Candidate experience:

- Invite landing page with organization branding.
- Assessment instructions and duration.
- Item navigation with progress.
- Existing coding workspace adapted to assessment mode.
- Question challenge renderer.
- Final submit confirmation.
- Completion page.

Assessment mode workspace differences:

- Hide public catalog navigation.
- Disable redo.
- Disable candidate custom tests for v1.
- Preserve integrity events.
- Lock final submission.
- Show assessment timer and item timer.

### Results And Review

Reports should include:

- Candidate profile and assignment status.
- Score breakdown by item.
- Coding challenge test results, coverage, style feedback.
- Question answers and auto/manual scores.
- Code snapshots.
- Integrity timeline.
- Review notes.
- Export CSV/PDF later.

## Assessment And Question Types

### Code Challenge Item

Uses current challenge runner.

Configurable by admin:

- Instructions
- Starter code
- Public tests
- Hidden tests
- Coverage threshold
- Time limit
- Points
- Runtime version
- Allowed languages, if multi-language challenge variants are later supported

### Multiple Choice

Auto-scored.

Fields:

- Prompt
- Options
- Single correct option
- Explanation
- Points

### Multi-Select

Auto-scored or partially scored.

Fields:

- Prompt
- Options
- Correct options
- Partial credit policy
- Points

### Short Answer

Manual or AI-assisted scoring later.

Fields:

- Prompt
- Expected answer
- Rubric
- Points

### Essay / System Design

Manual review.

Fields:

- Prompt
- Rubric criteria
- Max word count
- Attachments or diagrams later
- Points

### SQL Challenge

Can be implemented later as either:

- A code runner language/runtime for SQL/Postgres.
- A special question challenge with query execution and fixtures.

## Email And Invitation Flow

### Invite Candidate

1. Org admin selects an assessment version.
2. Org admin enters candidate emails or uploads CSV.
3. System creates candidates if needed.
4. System creates assignments.
5. System sends invite emails with tokenized links.

### Invite Link

Token rules:

- Use high-entropy random token.
- Store only token hash.
- Set expiration.
- Mark assignment as `OPENED` on first valid view.
- Mint short-lived candidate session token after start.

### Reminders

Future flow:

- Send reminder before due date.
- Send expiration notice.
- Notify org admin when a candidate submits.

## Scoring Model

Recommended assessment-level score:

```text
assessment_score = sum(item_score) / sum(item_max_score)
```

For coding items:

- Hidden tests are the primary correctness gate.
- Coverage/style can either block or reduce score.
- Existing feedback categories can feed report details.

For question items:

- MCQ and multi-select can auto-score.
- Short answer and essay can require manual review.
- Manual review should create an audit event.

Support these status values:

- `NOT_STARTED`
- `IN_PROGRESS`
- `SUBMITTED`
- `AUTO_SCORED`
- `MANUAL_REVIEW_REQUIRED`
- `REVIEWED`

## Security And Tenant Isolation

Must-have checks:

- Every organization admin query filters by `organization_id`.
- Every organization admin mutation validates membership.
- Candidate tokens only access a single assignment.
- Super admin endpoints require `SUPER_ADMIN`.
- Invite tokens are hashed at rest.
- Published assessment snapshots are immutable.
- Candidate submissions cannot escape assignment context.
- Admin reports cannot fetch submissions from another org.
- Audit events are written for sensitive actions.

Recommended tests:

- Org admin from Org A cannot list Org B candidates.
- Org admin from Org A cannot fetch Org B attempt report.
- Candidate invite for one assignment cannot fetch another assignment.
- Suspended org cannot create assignments.
- Expired invite cannot start an attempt.
- Published assessment remains unchanged after draft edits.

## Real-World Scenarios To Support

### Hiring Pipeline

Recruiter or engineering admin assigns a test, candidates complete it, reviewers inspect code and question answers, and the hiring team exports a report.

### Bootcamp Admissions

Applicants receive timed screening assessments. Admins compare pass rate, completion time, and integrity signals.

### Classroom Or Cohort

Instructors create assessments for a cohort. Students are candidates, not organization admins. Reports show completion and weak topics.

### Internal Upskilling

An organization assigns learning assessments to employees. Results identify skill gaps by language and challenge category.

### Certification Exam

Strict timing, immutable versions, retake policy, audit logs, and optional identity verification.

### Take-Home Project

Longer deadline, fewer restrictions, richer rubric, reviewer notes, and code quality feedback.

### Multi-Stage Assessment

Stage 1: MCQ screen. Stage 2: coding challenge. Stage 3: system design question. Advancement can be manual or score-based later.

### Anonymous Review

Reviewers see code and answers without candidate name/email to reduce bias.

### ATS Export

Export candidate status and score to tools such as Greenhouse, Lever, Ashby, or a generic webhook.

### Accommodations

Admins grant extra time or alternative formats for specific candidates while preserving audit history.

## Delivery Phases

### Phase 1: Tenant Foundation

Checklist:

- [ ] Add `SUPER_ADMIN` and `ORG_ADMIN` roles.
- [ ] Add organizations table and entity.
- [ ] Add organization members table and entity.
- [ ] Add membership-aware authorization service.
- [ ] Add super admin organization APIs.
- [ ] Add org admin profile APIs.
- [ ] Add database migration from current `ADMIN`.
- [ ] Add tenant isolation backend tests.
- [ ] Add role-aware frontend navigation.

Acceptance:

- A super admin can create an organization.
- A super admin can create an org admin.
- An org admin can view only their organization.
- Existing platform admin still works after migration.

### Phase 2: Organization Profiles And Admin Profiles

Checklist:

- [ ] Add organization profile edit page.
- [ ] Add admin profile edit page.
- [ ] Add logo/profile photo URL or upload metadata.
- [ ] Add validation for URLs, emails, and required profile fields.
- [ ] Add audit events for organization profile updates.

Acceptance:

- Organization branding appears on admin pages.
- Admin profile includes photo, name, title, and timezone.
- Profile changes are tenant-scoped and audited.

### Phase 3: Challenge Collections And Variants

Checklist:

- [ ] Add global challenge collections.
- [ ] Add organization challenge collections.
- [ ] Add collection item ordering.
- [ ] Add challenge variants for org customization.
- [ ] Add UI to group existing code challenges.
- [ ] Add UI to customize challenge instructions and scoring settings.

Acceptance:

- Admin can create a collection from existing challenges.
- Admin can customize a challenge without mutating the global base challenge.
- Customized challenge can still run through existing runner pipeline.

### Phase 4: Assessment Builder V1

Checklist:

- [ ] Add assessments table.
- [ ] Add assessment items table.
- [ ] Add assessment draft APIs.
- [ ] Add assessment builder UI.
- [ ] Add validation before publish.
- [ ] Add immutable assessment versions.
- [ ] Add preview mode.

Acceptance:

- Org admin can create an assessment draft.
- Org admin can add coding challenges.
- Org admin can publish a version.
- Published version remains stable after draft edits.

### Phase 5: Candidate Assignments

Checklist:

- [ ] Add candidates table.
- [ ] Add assessment assignments table.
- [ ] Add secure invite token generation and hashing.
- [ ] Add assignment APIs.
- [ ] Add email template for candidate invite.
- [ ] Add candidate invite landing page.
- [ ] Add attempt creation flow.
- [ ] Add assignment status transitions.

Acceptance:

- Org admin can assign an assessment by email.
- Candidate can open a secure invite link.
- Expired or cancelled invite cannot start.
- Org admin can see invited, opened, started, and submitted states.

### Phase 6: Assessment Attempt Runtime

Checklist:

- [ ] Add assessment attempts table.
- [ ] Add assessment item attempts table.
- [ ] Connect coding item attempts to submissions.
- [ ] Adapt workspace to assessment mode.
- [ ] Lock final submissions.
- [ ] Store code snapshots per attempt.
- [ ] Preserve integrity events in assessment reports.

Acceptance:

- Candidate can complete a coding challenge inside an assessment.
- Candidate cannot browse normal catalog during the attempt.
- Candidate final submission locks the attempt.
- Existing runner/SSE behavior still works.

### Phase 7: Question Challenges

Checklist:

- [ ] Add question challenges table.
- [ ] Add MCQ renderer.
- [ ] Add multi-select renderer.
- [ ] Add short answer renderer.
- [ ] Add essay/system-design renderer.
- [ ] Add auto-score support for MCQ/multi-select.
- [ ] Add manual scoring support.
- [ ] Add question item reports.

Acceptance:

- Org admin can add question challenges to an assessment.
- Candidate can answer questions.
- Auto-scored questions produce scores.
- Manual-review questions appear in the review queue.

### Phase 8: Reports, Review, And Exports

Checklist:

- [ ] Add assessment result dashboard.
- [ ] Add candidate attempt report.
- [ ] Add item-level scoring breakdown.
- [ ] Add reviewer notes.
- [ ] Add manual score update API.
- [ ] Add CSV export.
- [ ] Add PDF export later.
- [ ] Add audit events for score changes.

Acceptance:

- Org admin can review candidate code, answers, scores, and integrity events.
- Score changes are audited.
- Reports are scoped to the organization.

### Phase 9: Production Hardening

Checklist:

- [ ] Add rate limits for invite endpoints.
- [ ] Add resend limits for emails.
- [ ] Add org suspension behavior.
- [ ] Add data retention settings.
- [ ] Add audit log viewer.
- [ ] Add monitoring metrics for assignments and attempts.
- [ ] Add backup/restore notes for multi-tenant deployments.
- [ ] Add load testing for concurrent assessment starts.

Acceptance:

- Tenant isolation tests cover the highest-risk endpoints.
- Invite abuse is rate-limited.
- Suspended organizations cannot create new candidate attempts.
- Platform admins can inspect audit history.

## Testing Strategy

### Backend

- Repository tests for org-scoped queries.
- Service tests for membership checks.
- Controller/security tests for role boundaries.
- Assessment version immutability tests.
- Candidate token lifecycle tests.
- Submission integration tests for assessment-mode submissions.

### Frontend

- Route guard tests for super admin, org admin, and candidate pages.
- Assessment builder interaction tests.
- Candidate invite flow tests.
- Report page rendering tests.
- Visual checks for mobile candidate assessment pages.

### End-To-End

Critical path:

1. Super admin creates org.
2. Super admin creates org admin.
3. Org admin creates and publishes assessment.
4. Org admin assigns candidate by email.
5. Candidate starts assessment.
6. Candidate submits coding item.
7. Candidate completes assessment.
8. Org admin reviews report.

## Open Product Questions

- Should solo learner mode remain available after multi-tenant mode ships?
- Should candidates ever have reusable accounts, or remain invite-only?
- Should an org admin be allowed to belong to multiple organizations?
- Should assessments support retakes in v1?
- Should candidates see results after completion?
- Should question challenges be global, organization-specific, or both?
- Should custom candidate tests be disabled in all scored assessments?
- What storage backend should be used for logos and profile photos in production?
- What integrity signals are acceptable for your target audience?

## Suggested First Engineering Ticket

Create the tenant foundation:

- Add `SUPER_ADMIN` and `ORG_ADMIN`.
- Add `organizations`.
- Add `organization_members`.
- Add `OrganizationMembershipService`.
- Add super admin CRUD endpoints for organizations.
- Add tests proving org admins cannot access organizations they do not belong to.

This is the best first step because every later feature depends on secure tenant boundaries.
