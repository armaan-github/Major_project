# Reception Login API Notes

This document explains what the current implementation does when a receptionist logs in, and which APIs become available after that login.

## What the reception login does

Reception login is handled by the shared staff auth endpoint:

- `POST /api/hotelflow/auth/staff-login`

The frontend sends a JSON body with the receptionist username and password:

```json
{
  "userName": "reception",
  "password": "reception123"
}
```

In the current implementation, the backend:

1. Looks up the staff record by username.
2. Verifies the password.
3. Creates a session token.
4. Returns the token, user id, and role.

Example response:

```json
{
  "token": "<uuid-token>",
  "userId": "<staff-id>",
  "role": "RECEPTION"
}
```

The login is not a separate reception-only endpoint. It is the generic staff login, and the receptionist is identified by the `RECEPTION` role.

## Seeded receptionist account

The app seeds a default receptionist account when the application starts:

- Username: `reception`
- Password: `reception123`
- Role: `RECEPTION`

## How the token is used

After login, the receptionist must send the token on every protected request:

```http
Authorization: Bearer <token>
```

The auth interceptor checks the token and then verifies that the current endpoint allows the `RECEPTION` role.

## APIs a receptionist can use

These are the endpoints that the current implementation allows for the `RECEPTION` role.

### Room management

- `POST /api/hotelflow/room/create` - create a new room
- `GET /api/hotelflow/room/available` - list available rooms

### Booking flow

- `POST /api/hotelflow/booking/create` - create a booking for a guest
- `POST /api/hotelflow/booking/checkin` - check a guest into a booking
- `GET /api/hotelflow/booking/guest/{guestId}` - view bookings for a guest

### Folio and checkout

- `GET /api/hotelflow/folio/{bookingId}` - view the folio for a booking
- `POST /api/hotelflow/checkout` - generate the checkout invoice
- `POST /api/hotelflow/invoice/pay` - settle an invoice payment

### Dashboards

- `GET /api/hotelflow/dashboard/reception` - reception summary dashboard
- `GET /api/hotelflow/dashboard/guest/{guestId}` - guest dashboard, also visible to reception

### Live updates

- `GET /api/hotelflow/events/subscribe?channel=all` - subscribe to SSE updates

## What each reception API is used for

### `room/create`

Reception can add a room record with room number, room type, capacity, and nightly rate. The new room is saved as `AVAILABLE`.

### `room/available`

Reception can see all rooms currently marked `AVAILABLE` and use them during booking.

### `booking/create`

Reception creates a reservation by linking a guest to a room for a check-in and check-out date.

### `booking/checkin`

Reception checks the guest into an existing booking. This is the point where the booking moves into the in-stay flow.

### `booking/guest/{guestId}`

Reception can look up all bookings for a guest, which helps when managing arrival history or confirming an active reservation.

### `folio/{bookingId}`

Reception can view the running folio for a booking, including room charges and service charges.

### `checkout`

Reception finalizes the stay and generates the invoice for the booking.

### `invoice/pay`

Reception records payment against an invoice.

### `dashboard/reception`

Reception sees the operational summary for the front desk. The current implementation returns counts for total rooms, available rooms, occupied rooms, dirty rooms, and pending check-ins.

### `dashboard/guest/{guestId}`

Reception can also view a guest dashboard, which includes the guest’s bookings and running bill.

### `events/subscribe`

Reception can subscribe to live SSE events so the UI can refresh when bookings, check-ins, invoices, or inventory events happen.

## Quick summary

Reception login currently does four things:

1. Authenticates the default receptionist account.
2. Returns a bearer token with the `RECEPTION` role.
3. Unlocks reception-protected booking, room, folio, checkout, dashboard, and SSE APIs.
4. Lets the frontend use the token to call those APIs through the shared auth interceptor.
