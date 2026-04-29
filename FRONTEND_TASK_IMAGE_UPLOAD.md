# Frontend Task — Fix Menu Item Image Upload

**Assigned to:** Frontend Team  
**Priority:** 🔴 High (currently blocking "Add to Menu" feature)  
**File to edit:** `src/app/kitchen/kitchen-menu/kitchen-menu.component.ts`

---

## Problem

Right now when the kitchen staff picks a local image file, the code converts it to a
**base64 string** and sends it directly to the backend as `imageUrl`.

This causes the **"Failed to create item"** error because:
- A base64 image can be **1–3 MB of text**
- The MySQL `imageUrl` column is a normal `VARCHAR` — it cannot hold that much data
- The backend rejects the request

---

## What Needs to Be Done

### Step 1 — Get a free ImgBB API key

1. Go to 👉 [https://api.imgbb.com](https://api.imgbb.com)
2. Sign up for a free account
3. Copy your **API key**

---

### Step 2 — Replace the API key placeholder in the component

Open `kitchen-menu.component.ts` and find this line (around line 37):

```typescript
private readonly IMGBB_API_KEY = 'YOUR_IMGBB_API_KEY_HERE';
```

Replace `'YOUR_IMGBB_API_KEY_HERE'` with your actual key:

```typescript
private readonly IMGBB_API_KEY = 'abc123youractualkey';
```

---

### Step 3 — Disable the "Add to Menu" button while image is uploading

The component already has an `isUploadingImage` flag. You just need to apply it
to the submit button in the HTML template.

Open `kitchen-menu.component.html` and find the **"+ Add to Menu"** button.

Change it from:
```html
<button (click)="submitNewItem()" [disabled]="isSubmitting">
  + Add to Menu
</button>
```

To:
```html
<button (click)="submitNewItem()" [disabled]="isSubmitting || isUploadingImage">
  <span *ngIf="isUploadingImage">⏳ Uploading image...</span>
  <span *ngIf="!isUploadingImage">+ Add to Menu</span>
</button>
```

> This prevents the user from submitting the form before the image has finished
> uploading to ImgBB and the real URL is ready.

---

### Step 4 — (Optional but nice) Show an upload spinner on the image preview

Inside the image preview area in `kitchen-menu.component.html`, add a small loading
indicator while `isUploadingImage` is true:

```html
<div *ngIf="isUploadingImage" class="upload-spinner">
  ⏳ Uploading image to cloud...
</div>
```

---

## How It Works After the Fix

```
User picks file
    → Preview shown immediately (local data URL)
    → Image uploaded to ImgBB in background
    → ImgBB returns a real URL like:
         https://i.ibb.co/abc123/chicken-chilli.png
    → That URL is stored in newItem.imageUrl
    → User clicks "Add to Menu"
    → Backend saves the short URL (safe for MySQL VARCHAR)
    → Done ✅
```

---

## Testing Checklist

- [ ] Pick a JPG/PNG image under 2 MB → item creates successfully
- [ ] The stored `imageUrl` in the response is an `https://i.ibb.co/...` URL (not base64)
- [ ] "Add to Menu" button is disabled while image is uploading
- [ ] Picking an image over 2 MB shows an error message
- [ ] Pasting a URL directly (Paste URL mode) still works as before

---

## Notes

- The **backend does NOT need any changes** — it already accepts `imageUrl` as a string
- The **"Paste URL"** mode already works correctly — this task is only about the file upload mode
- If ImgBB is not available, the user can always switch to "Paste URL" mode and use a direct image link
