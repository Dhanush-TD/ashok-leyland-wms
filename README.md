# Ashok Leyland Engine Warehouse Management System

React + Spring Boot + MySQL rebuild of the WMS described in `SRS_Document.md`,
`API_Documentation.md`, `Database_Schema.sql` (original Postgres version) and
`Algorithms_and_Optimization.md` from your friend's project package. Those
files only shipped design docs and empty `frontend/src` / `backend/app`
folders, so this is a from-scratch implementation of that spec on the stack
you asked for.

## Stack

- **Frontend**: React 18 + Vite, plain CSS (no framework), `react-router-dom`, `axios`
- **Backend**: Spring Boot 3.3 (Web, Data JPA, Security, WebSocket, Validation), JWT auth (jjwt), Apache POI for Excel
- **Database**: MySQL 8

## 1. Set up MySQL

```bash
mysql -u root -p < database/mysql_schema.sql
```

This creates the `ashok_leyland_wms` database, all tables, a 12x8 demo grid
(rows A–H, columns 1–12), three engine models, four sample stored engines
(`AL-ENG-2026-9041..9044` in row A — 9041/9042/9043 block 9044, matching the
worked example in the API doc), one unplaced spare engine for the placement
demo, and three demo users.

| Username      | Password      | Role       |
|---------------|---------------|------------|
| admin         | Password123   | ADMIN      |
| supervisor1   | Password123   | SUPERVISOR |
| operator1     | Password123   | OPERATOR   |

## 2. Run the backend

```bash
cd backend
# edit src/main/resources/application.properties if your MySQL
# username/password/port differ from the defaults (root/root/3306)
mvn spring-boot:run
```

Starts on `http://localhost:8080`. `spring.jpa.hibernate.ddl-auto=validate`
is intentional — the schema is owned by `mysql_schema.sql`, not Hibernate.

## 3. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Starts on `http://localhost:5173` and proxies `/api` and `/ws` to the
backend (see `vite.config.js`), so no CORS setup is needed in dev.

## What's implemented

- **JWT login** (`/api/v1/auth/login`) with role-based access (OPERATOR / SUPERVISOR / ADMIN)
- **Live cinema-grid warehouse map** (`/map`) — color-coded cells, click-through cell drawer, real-time updates over a plain WebSocket at `/ws/warehouse-updates`
- **Inbound placement workflow** (`/placement`) — scan an engine barcode, get the nearest available cell (Manhattan distance from the dock), scan the location barcode to confirm; mismatched scans are rejected and broadcast as an `ALERT_MISMATCH` event
- **Retrieval / relocation solver** (`/retrieval`) — implements Algorithm 1 from `Algorithms_and_Optimization.md`: detects engines blocking a target's aisle, greedily assigns each blocker its nearest free temp cell, and returns the full temp-relocate → extract → restore (LIFO) sequence
- **Bulk Excel dispatch orders** — upload an `.xlsx` sheet of engine numbers and validate them against inventory

## What's stubbed / left for you to extend

The original spec (FR-05 audit trail UI, FR-08 role-based admin screens,
FR-09's full task-assignment push to operator handhelds, retrieval-order
persistence/approval workflow) is modeled in the database and entities but
doesn't have dedicated screens yet — the grid, placement, and single-engine
retrieval flows are the three you'll touch first, and the rest slots into
the same controller/service/page pattern.

## Project layout

```
database/mysql_schema.sql        MySQL DDL + seed data
backend/                         Spring Boot app (Maven)
  src/main/java/.../model/       JPA entities + enums
  src/main/java/.../repository/  Spring Data repositories
  src/main/java/.../service/     Business logic (auth, placement, relocation algorithm, excel)
  src/main/java/.../controller/  REST endpoints
  src/main/java/.../security/    JWT filter + util
  src/main/java/.../websocket/   Live grid update broadcaster
frontend/                        React + Vite app
  src/pages/                     Login, WarehouseMap, Placement, Retrieval
  src/components/                Sidebar, Topbar, GridCell, CellDrawer
  src/context/                   Auth context (JWT session)
  src/api/                       Axios client
```
