# HotelFlow UI Handoff

This document is a frontend-focused handoff for the HotelFlow backend. Give this to your UI dev (or UIX pilot) to build the application screens, routes, and interactions.

## Goals
- Deliver role-driven UI for Guest, Reception, Kitchen, and Manager.
- Map every primary user action to backend endpoints and expected payloads.
- Describe auth/token handling and realtime update integration (SSE).
- Provide page/component list, routes, data models, and an implementation checklist.

## Quick References
- Backend base URL: `http://localhost:8080`
- Postman collection: `postman/HotelFlow.postman_collection.json`
- Key backend files: [src/main/java/com/cs5500/NEUEat/controller/HotelFlowController.java](src/main/java/com/cs5500/NEUEat/controller/HotelFlowController.java)
- Seeding: [src/main/java/com/cs5500/NEUEat/config/HotelFlowDataSeeder.java](src/main/java/com/cs5500/NEUEat/config/HotelFlowDataSeeder.java)

## Roles & Primary Journeys

1. Guest (self-service)
   - Signup -> Login -> Book room (via reception in demo) -> Check-in status visible -> Place QR order -> View folio -> Checkout (optional)
   - Key endpoints: `POST /auth/guest-login`, `POST /guest/register`, `POST /order/qr`, `GET /folio/{bookingId}`, `GET /dashboard/guest/{guestId}`

2. Reception
   - Login -> Create room -> Create booking for guest -> Check in guest -> Start checkout -> Pay invoice
   - Key endpoints: `POST /auth/staff-login`, `POST /room/create`, `POST /booking/create`, `POST /booking/checkin`, `POST /checkout`, `POST /invoice/pay`, `GET /dashboard/reception`

3. Kitchen
   - Login -> View KDS queue -> Update order status -> See inventory low-stock
   - Key endpoints: `POST /auth/staff-login`, `GET /kds/queue`, `POST /order/status`, `GET /dashboard/kitchen`, `GET /inventory/low-stock`

4. Manager
   - Login -> View manager dashboard -> Manage inventory -> Monitor revenue and low-stock alerts
   - Key endpoints: `POST /auth/staff-login`, `GET /dashboard/manager`, `POST /inventory/item`, `GET /inventory/low-stock`

## Pages & Routes (Suggested)
- Public
  - `/` — Landing/login choice
  - `/login/staff` — Staff login (reception/kitchen/manager)
  - `/login/guest` — Guest login
  - `/signup/guest` — Guest registration

- Guest
  - `/guest/dashboard` — Active booking + folio summary
  - `/guest/booking` — Booking details & check-in status
  - `/guest/qr-order` — QR ordering screen (menu + cart)
  - `/guest/folio` — Full folio lines and invoice

- Reception
  - `/reception/dashboard` — Metrics + quick actions
  - `/reception/rooms` — Create/list rooms
  - `/reception/bookings/new` — Create booking form
  - `/reception/checkin` — Check-in UI + search booking
  - `/reception/checkout` — Checkout & invoice payment

- Kitchen
  - `/kitchen/dashboard` — Kitchen metrics
  - `/kitchen/queue` — Order queue with status controls
  - `/kitchen/order/:orderId` — Order detail

- Manager
  - `/manager/dashboard` — Manager metrics
  - `/manager/inventory` — Add/view inventory & low-stock

## UI Components (high level)
- `AuthForm` (props: mode: 'guest'|'staff', onSuccess)
- `Dashboard` (role-specific, composition of widgets)
- `RoomForm` (create room)
- `BookingForm` (guest select, room select, dates)
- `CheckinPanel` (search by bookingId, check-in action)
- `FolioView` (list folio lines, totals)
- `QrOrderMenu` (menu list, add to cart)
- `Cart` (items, quantities, total)
- `KitchenQueue` (list of ServiceOrder items with status actions)
- `InventoryEditor` (add item, view stock)
- `SseEventHandler` (singleton service that wires SSE to app state)

Implementation notes: components should be small, testable, and driven by API calls. Keep a service layer that maps API calls to typed models.

## Data Models (frontend shape)
- Guest: { id, userName, phoneNumber, address, city, state, zip, profile }
- Room: { id, roomNumber, roomType, capacity, nightlyRate, status }
- Booking: { id, guestId, roomId, checkInDate, checkOutDate, status }
- ServiceOrder: { id, bookingId, items[], status, totalAmount }
- Folio: { id, bookingId, roomCharges, serviceCharges, total, lines[] }
- Invoice: { id, bookingId, amountDue, amountPaid, status }
- InventoryItem: { id, itemName, currentStock, reorderLevel }

## Auth & Token Handling
- Login endpoints return `{ token, userId, role }`.
- Store token in secure storage (short-term dev: sessionStorage; production: HttpOnly cookie behind auth gateway).
- Attach `Authorization: Bearer <token>` header to every protected request.
- On 401: redirect to login. On 403: show role error or suggest different role login.
- Token lifetime: in-memory sessions on server; tokens die on server restart.

## SSE (Realtime)
- Endpoint: `GET /api/hotelflow/events/subscribe?channel=all` with `Accept: text/event-stream` and `Authorization` header.
- Use `EventSource` (if using cookie-based auth) or a fetch-based SSE wrapper that supports custom headers (EventSource doesn't support custom headers; use an SSE polyfill or proxy endpoint if necessary). Note: because backend expects `Authorization: Bearer <token>` and EventSource cannot set headers, the frontend should either:
  - Use a reverse proxy on the same domain to add the header, OR
  - Use a small worker that opens the SSE and passes token in query param (less secure), OR
  - Use WebSocket bridge if you add it server-side.
- Events to listen for: `order.placed`, `order.status_changed`, `booking.checked_in`, `checkout.completed`, `invoice.paid`, `inventory.low_stock`.

## Mapping UI Actions -> API Calls (examples)
- Staff login: `POST /api/hotelflow/auth/staff-login` with `{ userName, password }` -> store token and role
- Create room: `POST /api/hotelflow/room/create` (Authorization: Bearer receptionToken)
- Create booking: `POST /api/hotelflow/booking/create` with `{ guestId, roomId, checkInDate, checkOutDate }`
- Place QR order: `POST /api/hotelflow/order/qr` (guest token) with `{ bookingId, items[] }`
- Update order status: `POST /api/hotelflow/order/status` (kitchen token) with `{ orderId, status }`
- Get folio: `GET /api/hotelflow/folio/{bookingId}` (guest/reception)
- Checkout: `POST /api/hotelflow/checkout` (reception)

## Error Handling & UX
- Show inline errors for 4xx responses (map message to field). Backend currently sends plain messages for known exceptions.
- Global error handler for 401/403 to navigate to login or show permission dialog.

## Sample UI Flows (concise)
1. Guest sign-up -> login -> landing page shows `guestDashboard` (fetch `/dashboard/guest/{guestId}`) -> place QR order -> on submit POST /order/qr -> update cart and show success
2. Reception login -> create room -> create booking -> checkin -> open `receptionDashboard` which subscribes to SSE and shows live counts
3. Kitchen login -> open queue -> click order -> `POST /order/status` to progress status -> SSE updates propagate to guest and reception
4. Manager login -> open inventory -> add item -> if stock <= reorderLevel backend publishes `inventory.low_stock` event

## Wireframe Suggestions
- Dashboards: top KPI row, left nav, center list/widget area, right detail panel
- Kitchen queue: simple list with time, table/room, items badge, action buttons
- QR order: menu cards with add-to-cart FAB, bottom cart summary, checkout button
- Folio view: grouped lines with subtotal/total and pay button

## Accessibility & UX
- All forms must validate client-side first.
- Keyboard accessible table rows for queue and bookings.
- Provide clear role switcher or logout to test different experiences.

## Implementation Checklist (deliverable)
- [ ] Implement auth forms and token storage
- [ ] Implement role-based routing and guards
- [ ] Implement core pages: booking, check-in, folio, checkout
- [ ] Implement kitchen queue and order updates
- [ ] Integrate SSE or proxy for realtime updates
- [ ] Add error handling and user feedback
- [ ] Wire Postman collection flows for manual testing

## Notes for UIX/Design Pilot
- You can use the Postman collection to prototype data flows (collection path: `postman/HotelFlow.postman_collection.json`).
- Seeder creates default staff accounts for development: `reception`, `kitchen`, `manager` with `reception123`, `kitchen123`, `manager123`.
- For SSE with Authorization header, consider adding a small backend endpoint that proxies the header or implement WebSocket if you want header-based auth.

---

If you want, I can now scaffold a minimal React app structure or an Angular module with routes and example service calls wired to these endpoints.