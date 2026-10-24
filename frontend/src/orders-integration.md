# Orders integration boundary

The uploaded Spring Boot backend currently has no order API or live event contract. The Orders page starts empty and never requests an invented endpoint.

When the backend adapter receives an authenticated order for the signed-in merchant, dispatch this browser event:

```js
window.dispatchEvent(new CustomEvent('foodie:merchant-order', {
  detail: { order: {
    id: 'ORD-123', date: '2026-10-24', time: '18:42',
    paymentType: 'Card', netPayment: 'LKR 2,450.00', status: 'New', items: [],
  } },
}))
```

Map the backend's actual fields and statuses in that adapter; the UI assumes no endpoint or response format. The dashboard listens while the merchant is signed in, and the adapter must scope events to that merchant. `date` should be `YYYY-MM-DD` for date filtering.

Incoming order events create notifications in the top bell menu. The merchant can open an alert to view Orders, dismiss one alert, or clear all. Sound controls are intentionally absent from Orders and can be added in Settings later.