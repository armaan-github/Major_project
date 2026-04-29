# HotelFlow API Documentation

Complete API reference for the HotelFlow backend. This document is for frontend developers implementing the Angular/React frontend.

## Base URL

```
http://localhost:8080
```

## Environment Variables (Postman/Frontend)

These should be stored in your frontend state or local storage:

- `baseUrl`: `http://localhost:8080`
- `guestToken`: Token received from guest login
- `receptionToken`: Token received from reception staff login
- `kitchenToken`: Token received from kitchen staff login
- `managerToken`: Token received from manager staff login
- `guestId`: ID of the logged-in guest
- `roomId`: ID of the selected room
- `bookingId`: ID of the active booking
- `orderId`: ID of the active service order
- `invoiceId`: ID of the invoice

## Authentication

All protected endpoints require:

```
Authorization: Bearer <token>
```

Where `<token>` is obtained from login endpoints.

---

## 1. GUEST REGISTRATION

### Register Guest

**Endpoint:** `POST /api/hotelflow/guest/register`

**Authentication:** None required

**Request Body:**

```json
{
  "userName": "guest1",
  "password": "secret",
  "phoneNumber": "1234567890",
  "address": "101 Main St",
  "city": "Boston",
  "state": "MA",
  "zip": "02110",
  "roomPreference": "quiet",
  "foodAllergies": "nuts",
  "notes": "late check-in"
}
```

**Response (201 Created):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userName": "guest1",
  "password": "hashed_password",
  "phoneNumber": "1234567890",
  "address": "101 Main St",
  "city": "Boston",
  "state": "MA",
  "zip": "02110",
  "type": "guest",
  "profile": {
    "roomPreference": "quiet",
    "foodAllergies": "nuts",
    "notes": "late check-in"
  }
}
```

**Use Case:** Guest self-signup. Save the returned `id` as `guestId` for later use.

---

## 2. AUTHENTICATION

### Staff Login

**Endpoint:** `POST /api/hotelflow/auth/staff-login`

**Authentication:** None required

**Request Body:**

```json
{
  "userName": "reception",
  "password": "reception123"
}
```

**Default Staff Accounts:**

| Role | Username | Password |
|------|----------|----------|
| Reception | reception | reception123 |
| Kitchen | kitchen | kitchen123 |
| Manager | manager | manager123 |

**Response (200 OK):**

```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "role": "RECEPTION"
}
```

**Use Case:** Staff login. Use the returned `token` in the `Authorization` header for all subsequent requests. Route to the appropriate dashboard based on the `role`.

---

### Guest Login

**Endpoint:** `POST /api/hotelflow/auth/guest-login`

**Authentication:** None required

**Request Body:**

```json
{
  "userName": "guest1",
  "password": "secret"
}
```

**Response (200 OK):**

```json
{
  "token": "550e8400-e29b-41d4-a716-446655440002",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "GUEST"
}
```

**Use Case:** Guest login. Use the token and route to the guest dashboard.

---

## 3. ROOMS

### Create Room

**Endpoint:** `POST /api/hotelflow/room/create`

**Authentication:** Bearer token (RECEPTION or MANAGER only)

**Request Body:**

```json
{
  "roomNumber": "501",
  "roomType": "Deluxe",
  "capacity": 2,
  "nightlyRate": 150.0
}
```

**Response (201 Created):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440003",
  "roomNumber": "501",
  "roomType": "Deluxe",
  "capacity": 2,
  "nightlyRate": 150.0,
  "status": "AVAILABLE"
}
```

**Use Case:** Create a new room in the system. Save the `id` as `roomId` for booking.

---

### Get Available Rooms

**Endpoint:** `GET /api/hotelflow/room/available`

**Authentication:** Bearer token (RECEPTION or MANAGER)

**Response (200 OK):**

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440003",
    "roomNumber": "501",
    "roomType": "Deluxe",
    "capacity": 2,
    "nightlyRate": 150.0,
    "status": "AVAILABLE"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440004",
    "roomNumber": "502",
    "roomType": "Standard",
    "capacity": 1,
    "nightlyRate": 100.0,
    "status": "AVAILABLE"
  }
]
```

**Use Case:** List all available rooms for booking. Reception staff uses this to see which rooms can be booked.

---

## 4. BOOKINGS

### Create Booking

**Endpoint:** `POST /api/hotelflow/booking/create`

**Authentication:** Bearer token (RECEPTION only)

**Request Body:**

```json
{
  "guestId": "550e8400-e29b-41d4-a716-446655440000",
  "roomId": "550e8400-e29b-41d4-a716-446655440003",
  "checkInDate": "2026-04-14",
  "checkOutDate": "2026-04-16"
}
```

**Response (201 Created):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440005",
  "guestId": "550e8400-e29b-41d4-a716-446655440000",
  "roomId": "550e8400-e29b-41d4-a716-446655440003",
  "checkInDate": "2026-04-14",
  "checkOutDate": "2026-04-16",
  "status": "CONFIRMED"
}
```

**Use Case:** Create a booking for a guest. Save the `id` as `bookingId`. Status is `CONFIRMED` until check-in.

---

### Check In

**Endpoint:** `POST /api/hotelflow/booking/checkin`

**Authentication:** Bearer token (RECEPTION only)

**Request Body:**

```json
{
  "bookingId": "550e8400-e29b-41d4-a716-446655440005"
}
```

**Response (200 OK):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440005",
  "guestId": "550e8400-e29b-41d4-a716-446655440000",
  "roomId": "550e8400-e29b-41d4-a716-446655440003",
  "checkInDate": "2026-04-14",
  "checkOutDate": "2026-04-16",
  "status": "CHECKED_IN"
}
```

**Use Case:** Check the guest in. Room status becomes `OCCUPIED` and folio starts accumulating room charges.

---

### Get Guest Bookings

**Endpoint:** `GET /api/hotelflow/booking/guest/{guestId}`

**Authentication:** Bearer token (GUEST, RECEPTION, or MANAGER)

**Response (200 OK):**

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440005",
    "guestId": "550e8400-e29b-41d4-a716-446655440000",
    "roomId": "550e8400-e29b-41d4-a716-446655440003",
    "checkInDate": "2026-04-14",
    "checkOutDate": "2026-04-16",
    "status": "CHECKED_IN"
  }
]
```

**Use Case:** Get all bookings for a specific guest. Used by reception or guest to view their bookings.

---

## 5. SERVICE ORDERS (QR Ordering)

### Place QR Order

**Endpoint:** `POST /api/hotelflow/order/qr`

**Authentication:** Bearer token (GUEST only)

**Request Body:**

```json
{
  "bookingId": "550e8400-e29b-41d4-a716-446655440005",
  "items": [
    {
      "itemName": "Club Sandwich",
      "quantity": 2,
      "unitPrice": 12.5
    },
    {
      "itemName": "Coffee",
      "quantity": 1,
      "unitPrice": 4.0
    }
  ]
}
```

**Response (201 Created):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440006",
  "bookingId": "550e8400-e29b-41d4-a716-446655440005",
  "items": [
    {
      "itemName": "Club Sandwich",
      "quantity": 2,
      "unitPrice": 12.5
    },
    {
      "itemName": "Coffee",
      "quantity": 1,
      "unitPrice": 4.0
    }
  ],
  "status": "PLACED",
  "totalAmount": 33.0
}
```

**Use Case:** Guest places a QR order from their room. Save the `id` as `orderId`. The charge is automatically added to the guest's folio.

---

### Update Order Status

**Endpoint:** `POST /api/hotelflow/order/status`

**Authentication:** Bearer token (KITCHEN or MANAGER)

**Request Body:**

```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440006",
  "status": "IN_PREPARATION"
}
```

**Response (200 OK):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440006",
  "bookingId": "550e8400-e29b-41d4-a716-446655440005",
  "items": [...],
  "status": "IN_PREPARATION",
  "totalAmount": 33.0
}
```

**Valid Status Values:**

- `PLACED`: Initial status when order is created
- `IN_PREPARATION`: Kitchen is preparing the order
- `READY`: Order is ready for delivery/pickup
- `SERVED`: Order has been served/delivered

**Use Case:** Kitchen staff updates the order status as they prepare it. The UI should reflect these changes in real-time via SSE events.

---

### Get Kitchen Queue

**Endpoint:** `GET /api/hotelflow/kds/queue`

**Authentication:** Bearer token (KITCHEN or MANAGER)

**Response (200 OK):**

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440006",
    "bookingId": "550e8400-e29b-41d4-a716-446655440005",
    "items": [
      {
        "itemName": "Club Sandwich",
        "quantity": 2,
        "unitPrice": 12.5
      }
    ],
    "status": "PLACED",
    "totalAmount": 33.0
  }
]
```

**Use Case:** Kitchen staff views all orders waiting to be prepared. This is their main work queue.

---

## 6. FOLIO AND CHECKOUT

### Get Folio By Booking

**Endpoint:** `GET /api/hotelflow/folio/{bookingId}`

**Authentication:** Bearer token (GUEST, RECEPTION, or MANAGER)

**Response (200 OK):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440007",
  "bookingId": "550e8400-e29b-41d4-a716-446655440005",
  "roomCharges": 300.0,
  "serviceCharges": 33.0,
  "total": 333.0,
  "lines": [
    {
      "description": "Room (2 nights @ $150/night)",
      "amount": 300.0
    },
    {
      "description": "Club Sandwich (2)",
      "amount": 25.0
    },
    {
      "description": "Coffee (1)",
      "amount": 4.0
    }
  ]
}
```

**Use Case:** Guest or reception views the unified folio showing room charges and all service charges. This is the running bill.

---

### Checkout

**Endpoint:** `POST /api/hotelflow/checkout`

**Authentication:** Bearer token (RECEPTION or MANAGER)

**Request Body:**

```json
{
  "bookingId": "550e8400-e29b-41d4-a716-446655440005"
}
```

**Response (200 OK):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440008",
  "bookingId": "550e8400-e29b-41d4-a716-446655440005",
  "amountDue": 333.0,
  "amountPaid": 0.0,
  "status": "OPEN"
}
```

**Use Case:** Finalize the stay. An invoice is created with the total amount due. Room status becomes `DIRTY`.

---

### Pay Invoice

**Endpoint:** `POST /api/hotelflow/invoice/pay`

**Authentication:** Bearer token (RECEPTION or MANAGER)

**Request Body:**

```json
{
  "invoiceId": "550e8400-e29b-41d4-a716-446655440008",
  "amount": 333.0
}
```

**Response (200 OK):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440008",
  "bookingId": "550e8400-e29b-41d4-a716-446655440005",
  "amountDue": 333.0,
  "amountPaid": 333.0,
  "status": "PAID"
}
```

**Use Case:** Process payment for the invoice. If the amount covers the full bill, status becomes `PAID`.

---

## 7. DASHBOARDS

### Reception Dashboard

**Endpoint:** `GET /api/hotelflow/dashboard/reception`

**Authentication:** Bearer token (RECEPTION only)

**Response (200 OK):**

```json
{
  "totalBookings": 5,
  "checkedInCount": 3,
  "pendingCheckouts": 2,
  "totalRooms": 10,
  "occupiedRooms": 4,
  "availableRooms": 6
}
```

**Use Case:** Reception staff sees high-level metrics: bookings, check-ins, checkouts, room availability.

---

### Kitchen Dashboard

**Endpoint:** `GET /api/hotelflow/dashboard/kitchen`

**Authentication:** Bearer token (KITCHEN only)

**Response (200 OK):**

```json
{
  "totalOrders": 15,
  "placedOrders": 3,
  "inPreparationOrders": 5,
  "readyOrders": 2,
  "servedOrders": 5
}
```

**Use Case:** Kitchen staff sees order count by status. Helps them understand workload.

---

### Manager Dashboard

**Endpoint:** `GET /api/hotelflow/dashboard/manager`

**Authentication:** Bearer token (MANAGER only)

**Response (200 OK):**

```json
{
  "totalRevenue": 5500.0,
  "totalBookings": 5,
  "checkedInCount": 3,
  "totalOrders": 15,
  "totalRooms": 10,
  "lowStockItemsCount": 2
}
```

**Use Case:** Manager sees comprehensive operational metrics: revenue, bookings, orders, rooms, inventory alerts.

---

### Guest Dashboard

**Endpoint:** `GET /api/hotelflow/dashboard/guest/{guestId}`

**Authentication:** Bearer token (GUEST, RECEPTION, or MANAGER)

**Response (200 OK):**

```json
{
  "activeBooking": {
    "id": "550e8400-e29b-41d4-a716-446655440005",
    "roomNumber": "501",
    "checkOutDate": "2026-04-16",
    "status": "CHECKED_IN"
  },
  "currentFolio": {
    "total": 333.0,
    "roomCharges": 300.0,
    "serviceCharges": 33.0
  },
  "recentOrders": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440006",
      "status": "IN_PREPARATION",
      "totalAmount": 33.0
    }
  ]
}
```

**Use Case:** Guest sees their active booking, running bill, and recent orders.

---

## 8. INVENTORY

### Add Inventory Item

**Endpoint:** `POST /api/hotelflow/inventory/item`

**Authentication:** Bearer token (MANAGER only)

**Request Body:**

```json
{
  "itemName": "Eggs",
  "currentStock": 10,
  "reorderLevel": 20
}
```

**Response (201 Created):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440009",
  "itemName": "Eggs",
  "currentStock": 10,
  "reorderLevel": 20
}
```

**Use Case:** Manager adds a new inventory item. If `currentStock <= reorderLevel`, a low-stock event is published.

---

### Get Low Stock Items

**Endpoint:** `GET /api/hotelflow/inventory/low-stock`

**Authentication:** Bearer token (MANAGER or KITCHEN)

**Response (200 OK):**

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440009",
    "itemName": "Eggs",
    "currentStock": 10,
    "reorderLevel": 20
  }
]
```

**Use Case:** Manager or kitchen staff views items that need reordering.

---

## 9. LIVE EVENTS (SSE)

### Subscribe to Events

**Endpoint:** `GET /api/hotelflow/events/subscribe?channel=all`

**Authentication:** Bearer token (any authenticated user)

**Headers:**

```
Accept: text/event-stream
```

**Response:** Server-Sent Events stream

**Event Types Sent:**

1. **booking.created**: New booking created
2. **booking.checked_in**: Guest checked in
3. **order.placed**: New QR order placed
4. **order.status_changed**: Order status updated
5. **checkout.completed**: Checkout completed
6. **invoice.paid**: Invoice payment processed
7. **inventory.low_stock**: Item stock is low

**Example Event:**

```
event: order.placed
data: {"orderId":"550e8400-e29b-41d4-a716-446655440006","bookingId":"550e8400-e29b-41d4-a716-446655440005","totalAmount":33.0}
```

**Use Case:** Frontend subscribes to real-time updates. When events are received, refresh the relevant UI components (dashboards, queue, folio, etc.).

**Implementation Notes:**

- Use `EventSource` in JavaScript/Angular
- Keep the connection open while the user is on the page
- Unsubscribe when the user leaves or logs out
- Filter events based on the user's role

---

## ERROR RESPONSES

All endpoints return error responses in this format:

**400 Bad Request:**

```json
{
  "error": "Invalid input"
}
```

**401 Unauthorized:**

```json
{
  "error": "Missing bearer token"
}
```

**403 Forbidden:**

```json
{
  "error": "Role not allowed for this endpoint"
}
```

**404 Not Found:**

```json
{
  "error": "Booking not found"
}
```

**500 Internal Server Error:**

```json
{
  "error": "An unexpected error occurred"
}
```

---

## SUMMARY OF ENDPOINTS BY ROLE

### Guest Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/hotelflow/guest/register` | Self-signup |
| POST | `/api/hotelflow/auth/guest-login` | Login |
| GET | `/api/hotelflow/booking/guest/{guestId}` | View own bookings |
| POST | `/api/hotelflow/order/qr` | Place QR order |
| GET | `/api/hotelflow/folio/{bookingId}` | View bill |
| GET | `/api/hotelflow/dashboard/guest/{guestId}` | View personal dashboard |
| GET | `/api/hotelflow/events/subscribe` | Live updates |

### Reception Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/hotelflow/auth/staff-login` | Login |
| POST | `/api/hotelflow/room/create` | Create room |
| GET | `/api/hotelflow/room/available` | List rooms |
| POST | `/api/hotelflow/booking/create` | Create booking |
| POST | `/api/hotelflow/booking/checkin` | Check in guest |
| GET | `/api/hotelflow/booking/guest/{guestId}` | View bookings |
| GET | `/api/hotelflow/folio/{bookingId}` | View folio |
| POST | `/api/hotelflow/checkout` | Initiate checkout |
| POST | `/api/hotelflow/invoice/pay` | Process payment |
| GET | `/api/hotelflow/dashboard/reception` | View dashboard |
| GET | `/api/hotelflow/events/subscribe` | Live updates |

### Kitchen Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/hotelflow/auth/staff-login` | Login |
| GET | `/api/hotelflow/kds/queue` | View order queue |
| POST | `/api/hotelflow/order/status` | Update order status |
| GET | `/api/hotelflow/inventory/low-stock` | View low stock |
| GET | `/api/hotelflow/dashboard/kitchen` | View dashboard |
| GET | `/api/hotelflow/events/subscribe` | Live updates |

### Manager Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/hotelflow/auth/staff-login` | Login |
| POST | `/api/hotelflow/room/create` | Create room |
| GET | `/api/hotelflow/room/available` | List rooms |
| POST | `/api/hotelflow/inventory/item` | Add inventory |
| GET | `/api/hotelflow/inventory/low-stock` | View low stock |
| GET | `/api/hotelflow/dashboard/manager` | View dashboard |
| GET | `/api/hotelflow/events/subscribe` | Live updates |

---

## IMPLEMENTATION CHECKLIST FOR FRONTEND

- [ ] Create login page (staff and guest)
- [ ] Create guest signup page
- [ ] Create role-based navigation
- [ ] Create room list page (reception)
- [ ] Create booking creation form (reception)
- [ ] Create check-in page (reception)
- [ ] Create guest dashboard
- [ ] Create QR order page (guest)
- [ ] Create folio/bill view
- [ ] Create checkout page (reception)
- [ ] Create kitchen queue page (kitchen)
- [ ] Create order detail panel
- [ ] Create reception dashboard
- [ ] Create kitchen dashboard
- [ ] Create manager dashboard
- [ ] Implement SSE event subscription
- [ ] Add real-time updates to dashboards
- [ ] Add logout functionality
- [ ] Store and refresh tokens
- [ ] Add CORS support (backend already configured)
