# HotelFlow Quick User Flow (Short Theory)

This file explains the basic HotelFlow flow in simple order.

## Who Are The Users?

- Guest: Person staying in a room. Can create booking, place QR order, view folio, and complete checkout flow.
- Reception/Admin Staff: Creates rooms, helps booking and check-in, initiates checkout.
- Kitchen Staff: Watches kitchen queue and updates order status.
- Manager: Tracks invoice/folio totals and low stock inventory signals.

## Important Note About Login

Current HotelFlow module does not have separate login endpoints yet.

- Guest identity is created using `POST /api/hotelflow/guest/register`.
- Flow uses IDs (`guestId`, `roomId`, `bookingId`) for now.
- Existing old project login endpoints still exist under `/api/customer`, `/api/driver`, and `/api/restaurant` (legacy module).

## First Endpoint To Hit

For a new HotelFlow run, start with:

1. `POST /api/hotelflow/guest/register`
2. `POST /api/hotelflow/room/create`
3. `POST /api/hotelflow/booking/create`

This prepares the core stay context.

## End-to-End Guest Journey (Recommended Call Order)

1. Register guest
- Endpoint: `POST /api/hotelflow/guest/register`
- Output: `guestId`

2. Create room
- Endpoint: `POST /api/hotelflow/room/create`
- Output: `roomId`

3. Create booking
- Endpoint: `POST /api/hotelflow/booking/create`
- Input: `guestId`, `roomId`, `checkInDate`, `checkOutDate`
- Output: `bookingId`

4. Check in
- Endpoint: `POST /api/hotelflow/booking/checkin`
- Result: Room becomes `OCCUPIED`, folio starts with room charges

5. Place QR service order
- Endpoint: `POST /api/hotelflow/order/qr`
- Input: `bookingId` + ordered items
- Result: Charge auto-posted to folio (Unified Guest Folio behavior)

6. Kitchen processing
- Endpoint (view queue): `GET /api/hotelflow/kds/queue`
- Endpoint (update status): `POST /api/hotelflow/order/status`

7. View running bill
- Endpoint: `GET /api/hotelflow/folio/{bookingId}`
- Result: Shows room + food/service charges together

8. Checkout and invoice
- Endpoint: `POST /api/hotelflow/checkout`
- Result: Consolidated invoice generated, room marked `DIRTY`

9. Pay invoice
- Endpoint: `POST /api/hotelflow/invoice/pay`

## Inventory (Smart Ops Baseline)

- Add item: `POST /api/hotelflow/inventory/item`
- Low stock view: `GET /api/hotelflow/inventory/low-stock`

This is the current base for predictive inventory extension later.

## Why This Matches The Core Idea

- Unified Folio: Room and QR orders are consolidated in one folio.
- Contactless Path: QR ordering is supported in-room.
- Staff Flow: Kitchen queue and status update endpoints are available.
- Checkout Outcome: Single invoice for the stay session.
