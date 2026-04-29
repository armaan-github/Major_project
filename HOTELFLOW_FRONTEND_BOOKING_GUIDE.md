# HotelFlow — Frontend Booking Guide

This guide describes the receptionist walk-in flow: how the frontend obtains `guestId` and `roomId`, example requests, Postman snippets, and small `fetch` examples.

## Overview
- Receptionist steps: 1) register or find guest -> obtain `guestId`; 2) list available rooms -> obtain `roomId` and `currentQuote`; 3) create booking using both ids.

## Important endpoints

- Register guest (returns `id`)

  POST /api/hotelflow/guest/register

  Request body example:

  {
    "firstName": "Alice",
    "lastName": "Guest",
    "phone": "+15551234567",
    "email": "alice@example.com"
  }

  Response (example):

  {
    "id": "5f8d7c2e-...",
    "firstName": "Alice",
    "lastName": "Guest",
    "phone": "+15551234567",
    "email": "alice@example.com"
  }

- List available rooms (includes `currentQuote`)

  GET /api/hotelflow/room/available

  Each item is a `RoomQuoteView` with fields: `id`, `roomNumber`, `roomType`, `capacity`, `nightlyRate`, `status`, `currentQuote`.

- Create booking

  POST /api/hotelflow/booking/create

  Request body example:

  {
    "guestId": "{{guestId}}",
    "roomId": "{{roomId}}",
    "checkInDate": "2026-04-14",
    "checkOutDate": "2026-04-16"
  }

  Notes: the backend will record a `quotedNightlyRate` at booking creation (use `currentQuote` when showing price to guest). If check-in happens before 12:00, an automatic early check-in fee may be added.

## Postman quick snippets

- Save `guestId` after register (Tests tab):

  pm.environment.set("guestId", pm.response.json().id);

- Pick first room from `GET /room/available` (Tests tab):

  const rooms = pm.response.json();
  if (rooms && rooms.length) pm.environment.set("roomId", rooms[0].id);

- Use `{{guestId}}` and `{{roomId}}` in the body for `POST /booking/create`.

## Minimal frontend `fetch` examples

- Register guest and store id:

```javascript
const registerGuest = async (guest) => {
  const res = await fetch('/api/hotelflow/guest/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(guest)
  });
  const data = await res.json();
  sessionStorage.setItem('guestId', data.id);
  return data;
};
```

- Load available rooms and show `currentQuote`:

```javascript
const loadRooms = async () => {
  const res = await fetch('/api/hotelflow/room/available');
  return res.json(); // array of RoomQuoteView
};
```

- Create booking (using stored `guestId`):

```javascript
const createBooking = async ({roomId, checkInDate, checkOutDate}) => {
  const payload = {
    guestId: sessionStorage.getItem('guestId'),
    roomId,
    checkInDate,
    checkOutDate
  };
  const res = await fetch('/api/hotelflow/booking/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  return res.json();
};
```

## Searching existing guest (optional)
- Use the booking/guest search endpoints if receptionist wants to find an existing guest by phone before registering a new one (the backend implements quick search by phone/guest id/booking id).

## Real-time updates (SSE)
- To receive live events (room status changes, check-ins), open an `EventSource` to the server SSE endpoint (the backend publishes `booking.checked_in` and `room.status.changed`). Use these events to update UI lists in real time.

## UI tips
- Display `currentQuote` prominently when receptionist selects a room; backend will lock the quoted rate at booking creation.\
- Persist `guestId` in component state or `sessionStorage` for the duration of the walk-in session.\
- Validate phone/email to help deduplicate guests before registering.

---

This file explains where to get `guestId` and `roomId` for the booking flow, with Postman and simple frontend examples.
