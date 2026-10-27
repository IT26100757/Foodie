// Temporary development fixtures. Remove these after the shared merchant API is available.
export const DEMO_RESTAURANTS = [
  { name: 'Burger House', slug: 'burger-house', type: 'Burgers · Fast Food', rating: '4.8 (1.2k)', time: '25–35 min', price: 'Rs. 150', color: 'red', icon: '🍔', marketingEnabled: true, marketingOffer: 'Free delivery on your first order' },
  { name: 'Pizza Palace', slug: 'pizza-palace', type: 'Pizza · Italian', rating: '4.6 (980)', time: '30–45 min', price: 'Rs. 200', color: 'coral', icon: '🍕', marketingEnabled: true, marketingOffer: 'Save 15% on pizza night' },
  { name: 'Noodle Express', slug: 'noodle-express', type: 'Noodles · Asian', rating: '4.7 (856)', time: '20–30 min', price: 'Rs. 150', color: 'tan', icon: '🍜', marketingEnabled: false },
  { name: 'Curry Corner', slug: 'curry-corner', type: 'Rice & Curry · Local', rating: '4.5 (642)', time: '25–40 min', price: 'Rs. 120', color: 'red', icon: '🍲', marketingEnabled: true, marketingOffer: 'Rs. 250 off orders over Rs. 1,500' },
  { name: 'Coastal Bites', slug: 'coastal-bites', type: 'Seafood · Sri Lankan', rating: '4.7 (720)', time: '30–40 min', price: 'Rs. 180', color: 'coral', icon: '🐟', marketingEnabled: false },
  { name: 'Green Bowl Cafe', slug: 'green-bowl-cafe', type: 'Healthy · Cafe', rating: '4.6 (488)', time: '20–30 min', price: 'Rs. 120', color: 'green', icon: '🥗', marketingEnabled: true, marketingOffer: 'Free smoothie with a bowl' },
  { name: 'Spice Route', slug: 'spice-route', type: 'Indian · Biryani', rating: '4.8 (1.4k)', time: '35–45 min', price: 'Rs. 200', color: 'red', icon: '🍛', marketingEnabled: false },
  { name: 'Sweet Corner', slug: 'sweet-corner', type: 'Desserts · Bakery', rating: '4.7 (930)', time: '20–30 min', price: 'Rs. 100', color: 'coral', icon: '🍰', marketingEnabled: true, marketingOffer: 'Buy 2 desserts, get 1 free' },
  { name: 'Kottu House', slug: 'kottu-house', type: 'Sri Lankan · Street food', rating: '4.5 (564)', time: '25–35 min', price: 'Rs. 130', color: 'tan', icon: '🍽️', marketingEnabled: false },
  { name: 'Brew & Bites', slug: 'brew-and-bites', type: 'Cafe · Beverages', rating: '4.6 (312)', time: '15–25 min', price: 'Rs. 100', color: 'green', icon: '☕', marketingEnabled: false },
]

export function mapMerchantRestaurant(record) {
  const name = record.name || record.restaurantName || record.merchantName || 'Restaurant'
  const slug = record.slug || String(record.id ?? name).toLowerCase().trim().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '')
  const rating = record.rating == null ? '' : String(record.rating)
  return {
    ...record,
    id: record.id ?? slug,
    name,
    slug,
    type: record.type || record.cuisine || record.category || record.description || '',
    bannerImage: record.bannerImage || record.coverImage || record.banner || record.cover || '',
    logoImage: record.logoImage || record.logoUrl || record.logo || '',
    rating,
    time: record.deliveryTime || record.estimatedDeliveryTime || record.time || '',
    price: record.deliveryFee != null ? `Rs. ${record.deliveryFee}` : record.price || '',
    icon: record.icon || '♨',
    color: record.color || 'coral',
  }
}

export async function fetchMerchantRestaurants() {
  const response = await fetch('/api/restaurants')
  if (!response.ok) throw new Error(`Restaurant API returned ${response.status}`)
  const records = await response.json()
  if (!Array.isArray(records)) throw new Error('Restaurant API must return a list')
  return records.map(mapMerchantRestaurant)
}
