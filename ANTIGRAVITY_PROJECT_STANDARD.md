# ANTIGRAVITY PROJECT STANDARD — Global Project Rule

## 0. PURPOSE
Apply to every Antigravity project: project-local skills, strict system-file protection, concrete roadmap/checkpoints for large projects, and persistent memory so a project can be fully closed and later resumed.


## 1. MANDATORY PRINCIPLES

### 1.1 Project-first
All project-specific config, memory, logs, plans, metadata, and skills must live inside the repository, preferably in `.agents/skills/`, `.agents/rules/`, and `.project/`. Important memory must never exist only in chat.

### 1.2 Never modify system files
Agent MUST NOT tự ý sửa/xóa/đổi tên file OS, registry, PATH/env global, shell profile, cấu hình global của IDE/tool, hay cài software/package/service toàn máy. Không truy cập dữ liệu ngoài project nếu không cần. Ưu tiên dependency/config/venv/container/skill project-local. Bất kỳ path ngoài project đều là `PROTECTED` cho tới khi user cấp phép rõ ràng. Không dùng `sudo`/elevated privilege trừ khi user yêu cầu. Nếu command có thể ảnh hưởng ngoài project, phải dừng để xin phép.

### 1.3 Command safety
Trước command destructive: kiểm tra working directory + target + đường dẫn; ưu tiên preview/dry-run. Không tự chạy `rm -rf`, format disk, registry modification, system-wide install, service manipulation hoặc tương đương.

## 2. AUTOMATIC PROJECT ANALYSIS

When starting a new project or opening a project with no established state for the first time:

1. Inspect the current repository.
2. Identify:
   - project type;
   - language/framework;
   - build system;
   - package manager;
   - target platform;
   - test/lint/type-check tooling;
   - database/backend, if any;
   - deployment setup, if any;
   - existing technical constraints.
3. Do not modify source code merely to explore the project.
4. Read important project conventions before changing code.

## 3. AUTOMATICALLY SELECT AND IMPORT AAS SKILLS

Canonical skill source:
https://github.com/sickn33/agentic-awesome-skills

### 3.1. Principles
The agent MUST identify relevant skills for the project before implementing complex areas or work that follows specialized procedures.

Do not install the entire AAS catalog into Antigravity just to be safe.
Select only skills that are genuinely relevant.

### 3.2. Installation location
Use project-local installation by default:
`.agents/skills/`

Do not automatically install AAS into any global/system location.

### 3.3. AAS workflow
For a new project:

A. Inspect the project first.
B. Determine the required skill list.
C. Preview/inspect the selection using AAS before installing.
D. Install only the selected skills.
E. Record the selected skills in `.project/skills.md`.
F. If an AAS version/release is pinned, record the corresponding version/commit.

Prefer an equivalent workflow:

`npx agentic-awesome-skills --path .agents/skills --skills <skill1,skill2,...> --dry-run`

Only remove `--dry-run` and install after the selection is reasonable.

Never automatically choose `--all`.

### 3.4. Skill discovery
Depending on the project, skills may cover:
- architecture/planning;
- brainstorming;
- systematic-debugging;
- frontend/backend development;
- mobile development;
- Godot/game development;
- testing;
- security;
- documentation;
- database;
- DevOps;
- performance;
- code review;
- UI/UX;
- domain-specific skills.

Import only skills that provide practical value to the project.

### 3.5. Do not blindly trust skills
Skills are supporting guidance only. The agent must read the skill content and continue to obey this project rule and its safety constraints.
If a skill requests an action that conflicts with Section 1, this rule takes precedence.

## 4. PROJECT MEMORY — MANDATORY

Every project must have persistent project memory.
Tạo thư mục:
`.project/`

Minimum files:

### `.project/PROJECT.md`
Contains:
- project goals;
- scope;
- overall architecture;
- stack;
- entry points;
- build/run/test instructions;
- important decisions;
- coding conventions.

### `.project/STATE.md`
This is the single source of current project state.
It must always reflect:
- current checkpoint;
- current task;
- next task;
- completed work;
- failing/incomplete work;
- blockers;
- test/build status;
- recently changed files/modules;
- uncommitted changes, if Git is used.

### `.project/ROADMAP.md`
Use for medium/large projects.
Must contain:
- milestones;
- checkpoint IDs;
- the goal of each checkpoint;
- completion conditions;
- corresponding tests/verification;
- dependencies between checkpoints.

### `.project/DECISIONS.md`
Record long-lived technical decisions:
- problem;
- chosen option;
- rationale;
- rejected alternatives;
- consequences/trade-offs.

### `.project/SESSION_LOG.md`
Each development session must have a short entry:
- timestamp;
- objective;
- work performed;
- result;
- errors encountered;
- decisions;
- checkpoint;
- next action.

Never store real secrets, tokens, passwords, cookies, or credentials in these memory files.

## 5. MEMORY SYNC — END OF EVERY SESSION

Before ending a session or before the user closes the project, the agent MUST update:
1. `.project/STATE.md`
2. `.project/SESSION_LOG.md`
3. `.project/DECISIONS.md` nếu có quyết định mới
4. `.project/ROADMAP.md` nếu checkpoint thay đổi

Agent phải đảm bảo một agent mới hoàn toàn có thể đọc các file này và hiểu project đang ở đâu mà không cần lịch sử chat cũ.

If a risky or incomplete change is in progress:
- record `IN_PROGRESS` status;
- record the files being changed;
- record the current issue;
- record the next safe step.

## 6. CHAT HISTORY — FALLBACK MECHANISM

Do not assume Antigravity will preserve or restore complete chat history.

Project persistent memory must live in Git/workspace files.

If the platform/IDE provides exportable chat history, an export may be stored in:
`.project/chat-history/`

Chat history is supplemental only; `.project/STATE.md` and the other memory files are the primary recovery source.

Never commit secrets or sensitive data into chat-history.

## 7. PROJECT RESUME — ONE SHORT COMMAND

The project must support resuming with one short command, by default:

`/resume`

When `/resume` is received, the agent MUST:

1. Read `.project/PROJECT.md`.
2. Read `.project/STATE.md`.
3. Read `.project/ROADMAP.md` if it exists.
4. Read the latest decisions in `.project/DECISIONS.md`.
5. Read the latest session in `.project/SESSION_LOG.md`.
6. Check working tree/status if the project uses Git.
7. Determine the current checkpoint.
8. Give an extremely brief status summary.
9. Propose/perform the next step recorded in STATE when no blocker exists.

Do not reset, roll back, or refactor the project merely because a session/chat has been reopened.

### Resume output format

`/resume` should respond in this format:

- Project:
- Checkpoint:
- Status:
- Most recently completed:
- Blocker:
- Next action:
- Related files:
- Verification to run:

Then continue the work without asking the user to retell project history.

## 8. PROJECT SIZE & ROADMAP

### Small
A detailed roadmap may be skipped if the work can be completed in a few simple tasks.
`STATE.md` is still required.

### Medium
Milestones + checkpoints are required.

### Large
A roadmap MUST be created before deep implementation.

Each checkpoint must have:
- unique ID, e.g. `CP-01`, `CP-02`;
- objective;
- scope;
- inputs/dependencies;
- expected output;
- acceptance criteria;
- verification/test;
- rollback/recovery note when appropriate.

The agent must not jump across multiple major checkpoints without updating STATE.

## 9. CHECKPOINT PROTOCOL

After each checkpoint:
1. Run appropriate verification.
2. Check relevant regressions.
3. Update `STATE.md`.
4. Update `ROADMAP.md`.
5. Write the session log.
6. If Git is used, create commits at stable milestones when appropriate.

Never declare a checkpoint “DONE” unless its acceptance criteria are met.

## 10. CODE CHANGE POLICY

Before changing code:
- read related code;
- find callers/affected areas;
- understand the current architecture;
- prefer small, verifiable changes;
- do not rewrite large areas merely because the current style differs from the agent’s preference.

Do not create new abstractions without a clear reason.
Do not remove working functionality in favor of another solution without verifying the replacement.

## 11. TEST / VERIFY FIRST

After changes:
- run appropriate tests;
- run build/type-check/lint if available;
- for games/apps/UI, perform appropriate runtime or smoke testing;
- for bug fixes, create or update a regression test when practical.

If verification is not possible, record that explicitly in STATE instead of assuming the change works.

## 12. GIT

If the repository uses Git:
- never delete history;
- never force-push;
- never rewrite commit history;
- never hard-reset to an older commit unless explicitly requested by the user;
- preserve user changes;
- check working tree status before major changes.

Never commit secrets.

## 13. FILE OWNERSHIP

The agent may automatically create/modify/delete files only within:
- the current project/repository;
- build/cache directories explicitly defined by the project;
- project-local skill directories explicitly allowed by this rule.

Any path outside the project is `PROTECTED` until explicitly authorized by the user.

## 14. MISSING INFORMATION
Read repository/docs/config/tests before asking questions. Do not ask for information that can be safely determined by inspecting the project.

## 15. PROJECT CLOSURE CONDITION

Before the session ends, the project must be in one of these states:

`READY_TO_CLOSE`
hoặc
`IN_PROGRESS`
hoặc
`BLOCKED`

and `STATE.md` must precisely describe how to continue.

Never leave an “in progress” state without a concrete next step.

## 16. NEW PROJECT BOOTSTRAP

If the project has no `.project/`, the agent must create the minimum memory structure:

`.project/
├── PROJECT.md
├── STATE.md
├── ROADMAP.md        # nếu project vừa/lớn
├── DECISIONS.md
└── SESSION_LOG.md
`

Then follow this workflow:

`inspect → classify → select AAS skills → dry-run → install selected skills → define roadmap/checkpoints → implement → verify → persist state`

## 17. RULE PRIORITY

Priority order:
1. System and data safety.
2. Direct user requirements.
3. This project rule.
4. Other rules/skills.
5. Agent optimization preferences.

No skill or other instruction may override the system-protection principles in Section 1.

## 18 SESSION DONE
Verify or explicitly record unverified status; update STATE, SESSION_LOG, and ROADMAP when needed; record the next action; never leave unauthorized changes outside the project.

## QUICK COMMANDS

`/resume`
→ Restore context from persistent project memory and continue the current checkpoint.

`/status`
→ Read STATE + Git status + verification status, then report briefly.

`/checkpoint`
→ Verify current changes and record a new checkpoint in memory.

`/save`
→ Synchronize all project memory after the current session.

---

## FINAL RULE

Every new session must be able to start from the repository + `.project/` without relying on the agent’s hidden memory or an open chat history.

Project files are the long-term source of truth.
Chat is a temporary working interface.
`STATE.md` is the bridge between sessions.
`ROADMAP.md` is the map for large projects.
`.agents/skills/` is where project-local skills live.
