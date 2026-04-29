# HotelFlow — Menu API Documentation

> **Base URL:** `http://localhost:8080/api/hotelflow`  
> **Auth:** All endpoints (except login) require a Bearer token in the `Authorization` header.  
> **Content-Type:** `application/json` for all requests with a body.

---

## Authentication

All menu endpoints require a valid session token. Obtain one by logging in first.

### POST `/auth/staff-login`
Login as staff (MANAGER, KITCHEN, RECEPTION).

**Request Body:**
```json
{
  "userName": "manager1",
  "password": "yourpassword"
}
```

**Response `200 OK`:**
```json
{
  "token": "abc123xyz...",
  "userId": "uuid-of-user",
  "role": "MANAGER"
}
```

**Usage:** Store the `token` and send it on every subsequent request:
```
Authorization: Bearer abc123xyz...
```

---

## Menu Data Model

Every menu item has the following fields:

| Field | Type | Description |
|---|---|---|
| `id` | `string (UUID)` | Auto-generated, unique identifier |
| `itemName` | `string` | Name of the dish |
| `category` | `string` | Group (e.g. `"Starter"`, `"Main Course"`, `"Dessert"`, `"Beverage"`) |
| `description` | `string` | Short description of the dish |
| `imageUrl` | `string` | URL to the dish image (can be empty `""`) |
| `price` | `number` | Price in the local currency |
| `available` | `boolean` | `true` = shown to guests, `false` = hidden |
| `displayOrder` | `number` | Sort order within the category (lower = first) |
| `createdAt` | `string (ISO 8601)` | Auto-set on creation |
| `updatedAt` | `string (ISO 8601)` | Auto-set on every save |

**Example item object:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "itemName": "Paneer Tikka",
  "category": "Starter",
  "description": "Grilled cottage cheese with spices",
  "imageUrl": "https://example.com/paneer.jpg",
  "price": 249.00,
  "available": true,
  "displayOrder": 1,
  "createdAt": "2026-04-29T18:00:00",
  "updatedAt": "2026-04-29T18:00:00"
}
```

---

## Endpoints

---

### 1. GET `/menu` — Get Active Menu (Guest View)

Returns only **available** items, sorted by `category → displayOrder → itemName`.

**Who can call:** `GUEST`, `KITCHEN`, `MANAGER`

**Request:**
```
GET /api/hotelflow/menu
Authorization: Bearer <token>
```

**Response `200 OK`:**
```json
[
  {
    "id": "uuid-1",
    "itemName": "Paneer Tikka",
    "category": "Starter",
    "description": "Grilled cottage cheese with spices",
    "imageUrl": "",
    "price": 249.00,
    "available": true,
    "displayOrder": 1,
    "createdAt": "2026-04-29T18:00:00",
    "updatedAt": "2026-04-29T18:00:00"
  },
  {
    "id": "uuid-2",
    "itemName": "Dal Makhani",
    "category": "Main Course",
    "description": "Creamy black lentils slow cooked overnight",
    "imageUrl": "",
    "price": 320.00,
    "available": true,
    "displayOrder": 1,
    "createdAt": "2026-04-29T18:00:00",
    "updatedAt": "2026-04-29T18:00:00"
  }
]
```

> 💡 **Frontend tip:** Use this endpoint to render the QR scan menu page for guests. Group items by `category` to display them in sections.

---

### 2. GET `/menu/all` — Get All Menu Items (Admin View)

Returns **all** items including unavailable ones. Useful for the manager's menu management screen.

**Who can call:** `KITCHEN`, `MANAGER`

**Request:**
```
GET /api/hotelflow/menu/all
Authorization: Bearer <token>
```

**Response `200 OK`:** Same array format as above, but includes items where `available: false`.

> 💡 **Frontend tip:** Use this for the admin/kitchen menu management page. Show a toggle for `available` to enable/disable items without deleting them.

---

### 3. POST `/menu/item` — Create a Menu Item

Creates a new menu item. All fields except `description`, `imageUrl`, `available`, and `displayOrder` are required.

**Who can call:** `KITCHEN`, `MANAGER`

**Request:**
```
POST /api/hotelflow/menu/item
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "itemName": "Paneer Tikka",
  "category": "Starter",
  "description": "Grilled cottage cheese with spices",
  "imageUrl": "https://example.com/paneer.jpg",
  "price": 249.00,
  "available": true,
  "displayOrder": 1
}
```

| Field | Required | Default |
|---|---|---|
| `itemName` | ✅ Yes | — |
| `category` | ✅ Yes | — |
| `price` | ✅ Yes | — |
| `description` | ❌ No | `""` |
| `imageUrl` | ❌ No | `""` |
| `available` | ❌ No | `true` |
| `displayOrder` | ❌ No | `0` |

**Response `200 OK`:** Returns the full created `MenuItem` object including the auto-generated `id`.

**Error `400 Bad Request`:** If required fields are missing.

---

### 4. DELETE `/menu/item/{id}` — Delete a Menu Item

Permanently deletes a menu item by its UUID.

**Who can call:** `KITCHEN`, `MANAGER`

**Request:**
```
DELETE /api/hotelflow/menu/item/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <token>
```

**Response `200 OK`:**
```json
{
  "status": "deleted",
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Error `400 Bad Request`:** If the ID does not exist.

> ⚠️ **Note:** This is a hard delete. Consider toggling `available: false` (via a future PATCH endpoint) instead of deleting, so order history is preserved.

---

## Role Access Summary

| Endpoint | GUEST | KITCHEN | MANAGER |
|---|:---:|:---:|:---:|
| `GET /menu` | ✅ | ✅ | ✅ |
| `GET /menu/all` | ❌ | ✅ | ✅ |
| `POST /menu/item` | ❌ | ✅ | ✅ |
| `DELETE /menu/item/{id}` | ❌ | ✅ | ✅ |

---

## Suggested Frontend Pages

### 🍽️ Guest Menu Page (QR Scan Flow)
- Call `GET /menu` after the guest scans the QR code.
- Group items by `category` and render as sections.
- Show `itemName`, `description`, `price`, and `imageUrl`.
- Each item should have an **Add to Order** button (quantity selector).
- Submitting the order calls `POST /order/qr` (see separate order API docs).

### 🧑‍🍳 Manager Menu Management Page
- Call `GET /menu/all` to show all items in a table/list.
- Each row should have:
  - A **toggle switch** for `available` (to show/hide from guests).
  - A **Delete** button that calls `DELETE /menu/item/{id}`.
- A **Create Item** form (modal or page) that calls `POST /menu/item`.
- Sort/filter by `category`.

---

## Error Responses

All errors follow the standard Spring Boot error format:

```json
{
  "timestamp": "2026-04-29T18:08:06.718+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "human-readable error message",
  "path": "/api/hotelflow/menu/item"
}
```

| HTTP Status | Cause |
|---|---|
| `400 Bad Request` | Missing required field, invalid ID |
| `403 Forbidden` | Token missing, expired, or insufficient role |
| `404 Not Found` | Wrong URL path |

---

## Postman Quick-Start

1. `POST /auth/staff-login` → copy `token`
2. Add header `Authorization: Bearer <token>` to all requests
3. `POST /menu/item` with the JSON body above → note the returned `id`
4. `GET /menu` → verify the item appears
5. `DELETE /menu/item/<id>` → verify it's removed
