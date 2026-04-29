# HotelFlow Frontend Guide

This document explains what has already been built in HotelFlow and how the frontend should use it.

## What HotelFlow Is

HotelFlow is the hotel-side version of the project. It covers:

- Guest registration and guest login
- Staff login for Reception, Kitchen, and Manager
- Room setup and booking
- Check-in and room stay management
- QR-based in-room ordering
- Kitchen queue and order status updates
- Unified folio for room charges plus service charges
- Checkout and invoice payment
- Inventory tracking and low-stock alerts
- Live updates using Server-Sent Events

## Roles In The System

### Guest

- Registers with the guest register endpoint
- Logs in with guest login
- Books a room through the hotel flow
- Places QR orders from the room
- Views folio and stay summary

### Reception

- Logs in with staff login
- Creates rooms
- Creates bookings for guests
- Checks guests in
- Starts checkout flow
- Can view reception dashboard

### Kitchen

- Logs in with staff login
- Views kitchen queue
- Updates service order status
- Can view kitchen dashboard

### Manager

- Logs in with staff login
- Views manager dashboard
- Adds inventory items
- Views low-stock items
- Can access all main operational screens

## Login And Registration Behavior

### Guest Registration

- Endpoint: `POST /api/hotelflow/guest/register`
- This endpoint creates a new guest record in the database.
- The guest password is hashed with BCrypt before saving.
- The frontend can use this for self-signup.

### Staff Login

- Endpoint: `POST /api/hotelflow/auth/staff-login`
- This is not a signup form.
- Reception, Kitchen, and Manager accounts already exist in the database through seed data.
- The frontend sends username and password.
- If valid, the backend returns a token, userId, and role.

### Guest Login

- Endpoint: `POST /api/hotelflow/auth/guest-login`
- Guest username and password are checked against the guest table.
- If valid, the backend returns a token.

### Important Auth Detail

- Staff accounts are auto-created at startup by the data seeder.
- The frontend does not need to insert them manually for normal use.
- Tokens are stored in memory on the backend, so they are lost when the server restarts.

## Default Staff Accounts

These default accounts are seeded if they do not already exist:

- Reception: `reception` / `reception123`
- Kitchen: `kitchen` / `kitchen123`
- Manager: `manager` / `manager123`

## Auth Request Pattern For Frontend

After login, the frontend must store the returned token and send it like this:

```http
Authorization: Bearer <token>
```

Use that token on all protected HotelFlow endpoints.

## Screens The Frontend Should Have

### Public Screens

- Guest signup page
- Guest login page
- Staff login page

### Guest Screens

- Guest dashboard
- Booking page
- Room details page
- QR order page
- Folio page
- Checkout summary page

### Reception Screens

- Reception dashboard
- Room creation page
- Booking creation page
- Check-in page
- Checkout and payment page

### Kitchen Screens

- Kitchen dashboard
- Kitchen queue page
- Order detail panel

### Manager Screens

- Manager dashboard
- Inventory page
- Low-stock page
- Operational summary page

## Main API Flow For The Frontend

### 1. Guest Signup

Use guest registration first if the guest is new.

- `POST /api/hotelflow/guest/register`

### 2. Staff Login Or Guest Login

Log in based on the role.

- `POST /api/hotelflow/auth/staff-login`
- `POST /api/hotelflow/auth/guest-login`

### 3. Reception Creates Room

- `POST /api/hotelflow/room/create`

### 4. Reception Creates Booking

- `POST /api/hotelflow/booking/create`

### 5. Reception Checks Guest In

- `POST /api/hotelflow/booking/checkin`

### 6. Guest Places QR Order

- `POST /api/hotelflow/order/qr`

### 7. Kitchen Processes Order

- `GET /api/hotelflow/kds/queue`
- `POST /api/hotelflow/order/status`

### 8. Guest Or Reception Views Folio

- `GET /api/hotelflow/folio/{bookingId}`

### 9. Reception Checks Out And Pays Invoice

- `POST /api/hotelflow/checkout`
- `POST /api/hotelflow/invoice/pay`

### 10. Manager Manages Inventory

- `POST /api/hotelflow/inventory/item`
- `GET /api/hotelflow/inventory/low-stock`

## Live Updates For The UI

HotelFlow includes a live event endpoint using SSE.

- `GET /api/hotelflow/events/subscribe?channel=all`

Use this to keep the UI updated when these things change:

- New bookings
- New QR orders
- Order status changes
- Inventory low-stock alerts
- Checkout events

This means the frontend can refresh dashboards without constant polling.

## Data The Frontend Should Expect

### Guest

- `id`
- `userName`
- `phoneNumber`
- `address`
- `city`
- `state`
- `zip`
- `profile`

### Room

- `id`
- `roomNumber`
- `roomType`
- `capacity`
- `nightlyRate`
- `status`

### Booking

- `id`
- `guest`
- `room`
- `checkInDate`
- `checkOutDate`
- `status`

### Service Order

- `id`
- `booking`
- `items`
- `status`
- `totalAmount`

### Folio

- `booking`
- `roomCharges`
- `serviceCharges`
- `total`

### Invoice

- `id`
- `booking`
- `amountDue`
- `amountPaid`
- `status`

### Inventory Item

- `id`
- `itemName`
- `currentStock`
- `reorderLevel`

## State Changes The UI Should Reflect

### Room Status

- `AVAILABLE`
- `OCCUPIED`
- `DIRTY`

### Service Order Status

- `PLACED`
- `IN_PREPARATION`
- `READY`
- `SERVED`

### Invoice Status

- `OPEN`
- `PAID`

## Frontend Implementation Notes

- Store the token after login in local storage, session storage, or your app state layer.
- Send the token on every secured HotelFlow request.
- Build role-based routing so each login lands on the correct dashboard.
- Use the SSE endpoint to update dashboards in real time.
- Treat guest signup and staff login as separate paths.
- Do not build a staff signup form unless you also add a backend endpoint for creating staff users.

## Suggested UI Flow

1. Open app
2. Choose guest login, guest signup, or staff login
3. If staff login succeeds, route by role:
   - Reception -> reception dashboard
   - Kitchen -> kitchen dashboard
   - Manager -> manager dashboard
4. If guest login succeeds, route to guest dashboard
5. Use the dashboard to continue booking, ordering, folio, and checkout

## Backend Behavior Summary

- Guest registration is open
- Staff users are seeded, not self-registered
- Passwords are BCrypt-hashed
- Login validates username and password against the database
- Successful login returns a token
- Protected APIs require `Authorization: Bearer <token>`
- Tokens are stored in memory, not in the database

## Files That Matter For Frontend Work

- [src/main/java/com/cs5500/NEUEat/controller/HotelFlowController.java](src/main/java/com/cs5500/NEUEat/controller/HotelFlowController.java)
- [src/main/java/com/cs5500/NEUEat/service/hotelflow/HotelFlowAuthServiceImpl.java](src/main/java/com/cs5500/NEUEat/service/hotelflow/HotelFlowAuthServiceImpl.java)
- [src/main/java/com/cs5500/NEUEat/config/HotelFlowDataSeeder.java](src/main/java/com/cs5500/NEUEat/config/HotelFlowDataSeeder.java)
- [postman/HotelFlow.postman_collection.json](postman/HotelFlow.postman_collection.json)

## Short Answer

If you are building the frontend now, you do not need a staff signup screen for the current version. Use the seeded staff logins, guest signup for new guests, and bearer token auth for all protected pages.