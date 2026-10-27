// These preview records are intentionally separate from merchant data. They are
// used only when the customer offers endpoint is unavailable and are labelled
// as preview content by OffersPage.
export const MOCK_OFFER_FIXTURES = [
  { id: 'preview-pizza', restaurantName: 'Pizza Palace', restaurantRating: '4.6 (980)', cuisine: 'Pizza · Italian', itemName: 'Margherita Pizza', category: 'Pizza', title: '20% Off Margherita Pizza', description: 'Enjoy 20% off our classic Margherita Pizza with fresh ingredients.', offerType: '20% OFF', discountType: 'PERCENTAGE', discountValue: 20, price: 950, originalPrice: 1200, code: 'PIZZA20', conditions: 'Valid on one Margherita Pizza. Cannot be combined with another offer.', image: 'https://images.unsplash.com/photo-1579751626657-72bc17010498?auto=format&fit=crop&w=1000&q=85', expiresAt: '2026-12-31T23:59:00+05:30' },
  { id: 'preview-burger', restaurantName: 'Burger House', restaurantRating: '4.8 (1.2k)', cuisine: 'Burgers · Fast Food', itemName: 'Classic Burger', category: 'Burgers', title: 'Buy 1 Get 1 Free Burger', description: 'Get a free classic burger when you buy one. Limited time offer!', offerType: 'Buy 1 Get 1', discountType: 'BUY_X_GET_Y', discountValue: 1, price: 850, code: 'BURGER2', conditions: 'Purchase one Classic Burger to receive a second at no charge.', image: 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=1000&q=85', expiresAt: '2026-12-31T23:59:00+05:30' },
  { id: 'preview-noodles', restaurantName: 'Noodle Express', restaurantRating: '4.7 (856)', cuisine: 'Noodles · Asian', itemName: 'Chicken Noodles', category: 'Noodles', title: '15% Off Chicken Noodles', description: 'Delicious chicken noodles with authentic Asian flavours. Save 15% now!', offerType: '15% OFF', discountType: 'PERCENTAGE', discountValue: 15, price: 680, originalPrice: 800, code: 'NOODLE15', conditions: 'Valid on Chicken Noodles only.', image: 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=1000&q=85', expiresAt: '2026-12-31T23:59:00+05:30' },
  { id: 'preview-curry', restaurantName: 'Curry Corner', restaurantRating: '4.5 (642)', cuisine: 'Rice & Curry · Local', itemName: 'Rice & Curry orders', category: 'Rice & Curry', title: 'Free Delivery on Orders Over Rs. 1,000', description: 'Enjoy free delivery on all rice and curry orders above Rs. 1,000.', offerType: 'Free Delivery', discountType: 'FREE_DELIVERY', minimumOrder: 1000, code: 'CURRYRIDE', conditions: 'Minimum food order Rs. 1,000. Delivery fee discount applies at checkout.', image: 'https://images.unsplash.com/photo-1631452180519-c014fe946bc7?auto=format&fit=crop&w=1000&q=85', expiresAt: '2026-12-31T23:59:00+05:30' },
  { id: 'preview-seafood', restaurantName: 'Coastal Bites', restaurantRating: '4.7 (720)', cuisine: 'Seafood · Sri Lankan', itemName: 'Seafood Pizza', category: 'Pizza', title: '25% Off Seafood Pizza', description: 'Try our special seafood pizza at a discounted price. Limited time only!', offerType: '25% OFF', discountType: 'PERCENTAGE', discountValue: 25, price: 1250, originalPrice: 1650, code: 'COAST25', conditions: 'Valid on Seafood Pizza only.', image: 'https://images.unsplash.com/photo-1571407970349-bc81e7e96d47?auto=format&fit=crop&w=1000&q=85', expiresAt: '2026-12-31T23:59:00+05:30' },
  { id: 'preview-dessert', restaurantName: 'Sweet Corner', restaurantRating: '4.7 (930)', cuisine: 'Desserts · Bakery', itemName: 'Chocolate Cake', category: 'Desserts', title: 'Buy 2 Get 1 Free Cake', description: 'Choose any cakes and get one free. Perfect for sweet moments!', offerType: 'Buy 2 Get 1', discountType: 'BUY_X_GET_Y', discountValue: 2, price: 650, code: 'SWEET3', conditions: 'Add three eligible cakes to your order; the lowest priced cake is free.', image: 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=1000&q=85', expiresAt: '2026-12-31T23:59:00+05:30' },
  { id: 'preview-biryani', restaurantName: 'Spice Route', restaurantRating: '4.8 (1.4k)', cuisine: 'Indian · Biryani', itemName: 'Chicken Biryani', category: 'Rice & Curry', title: '20% Off Chicken Biryani', description: 'Authentic chicken biryani with rich spices. Save 20% on your order!', offerType: '20% OFF', discountType: 'PERCENTAGE', discountValue: 20, price: 1150, originalPrice: 1450, code: 'BIRYANI20', conditions: 'Valid on Chicken Biryani only.', image: 'https://images.unsplash.com/photo-1633945274405-b6c8069047b0?auto=format&fit=crop&w=1000&q=85', expiresAt: '2026-12-31T23:59:00+05:30' },
  { id: 'preview-drinks', restaurantName: 'Green Bowl Cafe', restaurantRating: '4.6 (488)', cuisine: 'Healthy · Cafe', itemName: 'Beverages', category: 'Beverages', title: '30% Off All Beverages', description: 'Enjoy 30% off all drinks including bubble tea, smoothies and more.', offerType: '30% OFF', discountType: 'PERCENTAGE', discountValue: 30, price: 350, originalPrice: 500, code: 'DRINK30', conditions: 'Valid on beverages only. One discount per order.', image: 'https://images.unsplash.com/photo-1558857563-b371033873b8?auto=format&fit=crop&w=1000&q=85', expiresAt: '2026-12-31T23:59:00+05:30' },
]

function normalizeOffer(offer) {
  const restaurant = offer.restaurant || {}
  const item = offer.menuItem || offer.item || {}
  return {
    ...offer,
    id: String(offer.id ?? offer.offerId ?? offer.code ?? crypto.randomUUID()),
    restaurantName: offer.restaurantName ?? restaurant.name ?? 'Restaurant',
    restaurantRating: offer.restaurantRating ?? restaurant.rating ?? '',
    cuisine: offer.cuisine ?? restaurant.cuisine ?? restaurant.type ?? '',
    itemName: offer.itemName ?? item.name ?? offer.targetName ?? 'Selected items',
    category: offer.category ?? item.category ?? offer.foodCategory ?? 'Others',
    title: offer.title ?? offer.name ?? offer.offerName ?? 'Special offer',
    description: offer.description ?? offer.detail ?? '',
    offerType: offer.offerType ?? offer.discountLabel ?? offer.discountType ?? 'Special offer',
    discountType: offer.discountType ?? offer.type ?? '',
    price: offer.price ?? item.price ?? null,
    originalPrice: offer.originalPrice ?? item.originalPrice ?? null,
    code: offer.code ?? offer.offerCode ?? '',
    conditions: offer.conditions ?? offer.terms ?? offer.description ?? '',
    image: offer.image ?? offer.imageUrl ?? item.imageUrl ?? restaurant.imageUrl ?? '',
    expiresAt: offer.expiresAt ?? offer.validUntil ?? offer.endDate ?? null,
    enabled: offer.enabled ?? offer.isEnabled ?? true,
    published: offer.published ?? offer.isPublished ?? offer.status === 'PUBLISHED',
  }
}

export async function loadCustomerOffers(signal) {
  const endpoint = import.meta.env.VITE_CUSTOMER_OFFERS_API_URL || '/api/customer/offers'
  try {
    const response = await fetch(endpoint, { headers: { Accept: 'application/json' }, signal })
    if (!response.ok) throw new Error(`Offers request failed (${response.status})`)
    const payload = await response.json()
    const records = Array.isArray(payload) ? payload : payload.offers ?? payload.content ?? []
    return { source: 'api', offers: records.map(normalizeOffer).filter((offer) => offer.enabled && offer.published) }
  } catch (error) {
    if (signal?.aborted) throw error
    return { source: 'preview', offers: MOCK_OFFER_FIXTURES.map(normalizeOffer) }
  }
}
