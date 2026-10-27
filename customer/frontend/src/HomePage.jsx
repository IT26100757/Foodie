import { useEffect, useRef, useState } from 'react'
import './HomePage.css'
import AccountCenter from './AccountCenter.jsx'
import { rewardPoints } from './rewardPoints.js'

const slides = [
  { src: '/banners/good-food-together.jpg', alt: 'Friends sharing a meal', background: '#c8edfb', kicker: 'GOOD THINGS ARE CLOSE', titleFirst: 'GOOD FOOD.', titleAccent: 'BRINGS PEOPLE TOGETHER.', copy: 'Share meals, create moments and support local communities.', primaryLabel: 'Start Ordering', primaryHref: '#restaurants', secondaryLabel: 'Learn More', secondaryHref: '#offers' },
  { src: '/banners/discover-local-favorites.jpg', alt: 'Local meals and restaurant favourites', background: '#def4df', kicker: 'GOOD THINGS ARE CLOSE', titleFirst: 'DISCOVER', titleAccent: 'LOCAL FAVOURITES.', copy: 'From home-cooked meals to popular spots, find food you’ll love.', primaryLabel: 'Browse Restaurants', primaryHref: '#restaurants', secondaryLabel: 'See Popular Dishes', secondaryHref: '#categories' },
  { src: '/banners/faster-delivery.jpg', alt: 'Food courier delivering an order', background: '#fde5df', kicker: 'GOOD THINGS ARE CLOSE', titleFirst: 'YOUR FAVOURITE', titleAccent: 'FOOD, FASTER.', copy: 'Quick delivery from local restaurants straight to your door.', primaryLabel: 'Order Now', primaryHref: '#restaurants', secondaryLabel: 'Browse Menu', secondaryHref: '#categories' },
  { src: '/banners/local-food-love-delivered.jpg', alt: 'Courier bringing local food to a restaurant customer', background: '#fff5df', kicker: 'GOOD THINGS ARE CLOSE', titleFirst: 'LOCAL FOOD.', titleAccent: 'LOVE, DELIVERED.', copy: 'Support neighbourhood kitchens and enjoy something wonderful.', primaryLabel: 'Meet Local Kitchens', primaryHref: '#restaurants', secondaryLabel: 'See Today’s Menu', secondaryHref: '#categories' },
]
const categories = [
  ['Appetizers', '🥟', 'cat-peach'], ['Main course', '🍛', 'cat-green'], ['Sides', '🍟', 'cat-yellow'],
  ['Desserts', '🍰', 'cat-pink'], ['Beverages', '🧋', 'cat-cream'], ['Other', '🍜', 'cat-green'],
]
const restaurants = [
  { name: 'Burger House', type: 'Burgers · Fast Food', rating: '4.8 (1.2k)', time: '25–35 min', price: 'Rs. 150', color: 'red', icon: '🍔' },
  { name: 'Pizza Palace', type: 'Pizza · Italian', rating: '4.6 (980)', time: '30–45 min', price: 'Rs. 200', color: 'coral', icon: '🍕' },
  { name: 'Noodle Express', type: 'Noodles · Asian', rating: '4.7 (856)', time: '20–30 min', price: 'Rs. 150', color: 'tan', icon: '🍜' },
  { name: 'Curry Corner', type: 'Rice & Curry · Local', rating: '4.5 (642)', time: '25–40 min', price: 'Rs. 120', color: 'red', icon: '🍲' },
  { name: 'Coastal Bites', type: 'Seafood · Sri Lankan', rating: '4.7 (720)', time: '30–40 min', price: 'Rs. 180', color: 'coral', icon: '🐟' },
  { name: 'Green Bowl Cafe', type: 'Healthy · Cafe', rating: '4.6 (488)', time: '20–30 min', price: 'Rs. 120', color: 'green', icon: '🥗' },
  { name: 'Spice Route', type: 'Indian · Biryani', rating: '4.8 (1.4k)', time: '35–45 min', price: 'Rs. 200', color: 'red', icon: '🍛' },
  { name: 'Sweet Corner', type: 'Desserts · Bakery', rating: '4.7 (930)', time: '20–30 min', price: 'Rs. 100', color: 'coral', icon: '🍰' },
  { name: 'Kottu House', type: 'Sri Lankan · Street food', rating: '4.5 (564)', time: '25–35 min', price: 'Rs. 130', color: 'tan', icon: '🍽️' },
  { name: 'Brew & Bites', type: 'Cafe · Beverages', rating: '4.6 (312)', time: '15–25 min', price: 'Rs. 100', color: 'green', icon: '☕' },
]

const featuredDishes = {
  'Burger House': { name: 'House Burger', price: 850, icon: '🍔' },
  'Pizza Palace': { name: 'Margherita Pizza', price: 950, icon: '🍕' },
  'Noodle Express': { name: 'Chicken Chow Mein', price: 780, icon: '🍜' },
  'Curry Corner': { name: 'Chicken Rice & Curry', price: 900, icon: '🍲' },
  'Coastal Bites': { name: 'Grilled Fish Rice', price: 1250, icon: '🐟' },
  'Green Bowl Cafe': { name: 'Garden Salad Bowl', price: 760, icon: '🥗' },
  'Spice Route': { name: 'Chicken Biryani', price: 1150, icon: '🍛' },
  'Sweet Corner': { name: 'Chocolate Cake', price: 650, icon: '🍰' },
  'Kottu House': { name: 'Cheese Chicken Kottu', price: 890, icon: '🍽️' },
  'Brew & Bites': { name: 'Iced Coffee', price: 550, icon: '☕' },
}
const availableOffers = [
  { title: 'Rs. 250 off your first order', detail: 'On food orders of Rs. 1,500 or more.', code: 'FOODIE250', icon: '🎉' },
  { title: 'Free delivery', detail: 'On orders over Rs. 1,000 from participating restaurants.', code: 'FREERIDE', icon: '🛵' },
  { title: '15% off pizza night', detail: 'Save on pizza orders every Friday, up to Rs. 500.', code: 'PIZZA15', icon: '🍕' },
]

function Brand() {
  return <a className="home-brand" href="/" aria-label="Foodie home"><span className="brand-burger">🍔</span><strong>foodie<span>.</span></strong><small>CUSTOMER</small></a>
}

function RestaurantArt({ name, color, icon }) {
  return <svg className={`restaurant-art art-${color}`} viewBox="0 0 360 112" aria-label={`${name} illustrated storefront`} role="img">
    <path d="M0 86h360v26H0z" fill="#d8c8aa"/><path d="M0 33 180 0l180 33v61H0z" fill="#628d65"/>
    <path d="M72 38h222v61H72z" fill="#f4d9a6"/><path d="M51 35h255v19H51z" fill="#a43e32"/><path d="M79 53h206v46H79z" fill="#fff1cf"/><path d="M102 61h54v38h-54zm78 0h73v38h-73z" fill="#c97c4b"/><path d="M0 73q45-22 88 2v37H0zm278-10q44-18 82 9v40h-82z" fill="#3e7750"/><path d="M34 35 47 10l13 25m274 2 11-26 14 28" stroke="#315c3e" strokeWidth="9" strokeLinecap="round"/><path d="M24 71v41m296-45v45" stroke="#76573a" strokeWidth="5"/><text x="180" y="49" textAnchor="middle" fontSize="18" fontFamily="sans-serif" fontWeight="800" fill="white">{name.toUpperCase()}</text><text x="126" y="91" fontSize="21">{icon}</text><circle cx="38" cy="62" r="15" fill="#75a66d"/><circle cx="322" cy="70" r="17" fill="#79a86e"/>
  </svg>
}

export default function HomePage() {
  const [activeSlide, setActiveSlide] = useState(0)
  const [search, setSearch] = useState('')
  const [searchOpen, setSearchOpen] = useState(false)
  const [activeSuggestion, setActiveSuggestion] = useState(0)
  const searchRef = useRef(null)
  const [showAllRestaurants, setShowAllRestaurants] = useState(false)
  const [offersOpen, setOffersOpen] = useState(false)
  const [offerNotice, setOfferNotice] = useState('')
  const [favoritePrompt, setFavoritePrompt] = useState(false)
  const [cart, setCart] = useState(() => { try { return JSON.parse(localStorage.getItem('foodie.cart') || '[]') } catch { return [] } })
  const [cartOpen, setCartOpen] = useState(false)
  const [orderConfirmation, setOrderConfirmation] = useState(null)
  const [profileOpen, setProfileOpen] = useState(false)
  const [accountSection, setAccountSection] = useState('')
  const [theme, setTheme] = useState(() => localStorage.getItem('foodie.customer.theme') === 'dark' ? 'dark' : 'light')
  const [customer, setCustomer] = useState(() => { try { return JSON.parse(sessionStorage.getItem('foodie.customer') || 'null') } catch { return null } })
  const [favorites, setFavorites] = useState(() => { try { const customerKey = customer && (customer.identifier || customer.displayName); const saved = customerKey && JSON.parse(localStorage.getItem('foodie.customer.favorites.' + encodeURIComponent(String(customerKey).trim().toLowerCase())) || '[]'); return Array.isArray(saved) ? saved : [] } catch { return [] } })
  const [profileData, setProfileData] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('foodie.customer.profile') || 'null') || {
        name: '', email: customer && customer.identifier && customer.identifier.includes('@') ? customer.identifier : '',
        phone: customer && customer.identifier && !customer.identifier.includes('@') ? customer.identifier.replace(/\D/g, '').replace(/^94/, '').replace(/^0/, '').slice(0, 9) : '',
        address1: '', address2: '', address3: '', photo: '',
      }
    } catch { return { name: '', email: '', phone: '', address1: '', address2: '', address3: '', photo: '' } }
  })
  const [orders, setOrders] = useState(() => { try { return JSON.parse(localStorage.getItem('foodie.customer.orders') || '[]') } catch { return [] } })
  const isGuest = new URLSearchParams(window.location.search).get('mode') === 'guest' || !customer
  const favoriteStorageKey = !isGuest && (customer?.identifier || customer?.displayName) ? 'foodie.customer.favorites.' + encodeURIComponent(String(customer.identifier || customer.displayName).trim().toLowerCase()) : ''

  useEffect(() => {
    const timer = window.setInterval(() => setActiveSlide((slide) => (slide + 1) % slides.length), 6500)
    return () => window.clearInterval(timer)
  }, [])
  useEffect(() => { localStorage.setItem('foodie.cart', JSON.stringify(cart)) }, [cart])
  useEffect(() => { localStorage.setItem('foodie.customer.orders', JSON.stringify(orders)) }, [orders])
  useEffect(() => { localStorage.setItem('foodie.customer.theme', theme) }, [theme])
  useEffect(() => {
    function closeSuggestions(event) { if (searchRef.current && !searchRef.current.contains(event.target)) setSearchOpen(false) }
    document.addEventListener('mousedown', closeSuggestions)
    return () => document.removeEventListener('mousedown', closeSuggestions)
  }, [])

  const slide = slides[activeSlide]
  const customerName = profileData.name.trim() || (customer?.displayName && customer.displayName !== 'Foodie customer' ? customer.displayName : '') || (customer?.identifier && customer.identifier.includes('@') ? customer.identifier.split('@')[0] : 'Customer')
  const searchTerm = search.trim().toLowerCase()
  const matchingRestaurants = restaurants.filter((restaurant) => { const dish = featuredDishes[restaurant.name]; return [restaurant.name, restaurant.type, dish.name].some((value) => value.toLowerCase().includes(searchTerm)) })
  const shownRestaurants = searchTerm || showAllRestaurants ? matchingRestaurants : matchingRestaurants.slice(0, 4)
  const searchSuggestions = searchTerm ? matchingRestaurants.slice(0, 6) : []
  const favoriteRestaurants = restaurants.filter((restaurant) => favorites.includes(restaurant.name))
  const itemCount = cart.reduce((total, item) => total + item.quantity, 0)
  const subtotal = cart.reduce((total, item) => total + item.price * item.quantity, 0)
  const money = (amount) => 'Rs. ' + amount.toLocaleString('en-LK')

  function addToCart(restaurant) {
    const dish = featuredDishes[restaurant.name]
    setCart((items) => {
      const existing = items.find((item) => item.id === restaurant.name)
      return existing
        ? items.map((item) => item.id === restaurant.name ? { ...item, quantity: item.quantity + 1 } : item)
        : [...items, { id: restaurant.name, name: dish.name, restaurant: restaurant.name, price: dish.price, quantity: 1 }]
    })
  }
  function selectSearchResult(restaurant) {
    setSearch(restaurant.name)
    setSearchOpen(false)
    setActiveSuggestion(0)
    window.setTimeout(() => document.getElementById('restaurants')?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 0)
  }
  function toggleFavorite(name) {
    if (isGuest || !favoriteStorageKey) { setFavoritePrompt(true); return }
    const next = favorites.includes(name) ? favorites.filter((item) => item !== name) : [...favorites, name]
    setFavorites(next)
    localStorage.setItem(favoriteStorageKey, JSON.stringify(next))
  }
  function renderRestaurantCard(restaurant) {
    const dish = featuredDishes[restaurant.name]
    const saved = favorites.includes(restaurant.name)
    return <article className="restaurant-card" key={restaurant.name}>
      <div className="restaurant-picture"><RestaurantArt name={restaurant.name} color={restaurant.color} icon={restaurant.icon} /><button type="button" aria-label={(saved ? 'Remove ' : 'Save ') + restaurant.name + (isGuest ? ' (sign in required)' : ' to favourites')} aria-pressed={saved} className={'favorite' + (saved ? ' is-favorite' : '')} onClick={() => toggleFavorite(restaurant.name)}><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20.8 8.7c0 5-8.8 11-8.8 11s-8.8-6-8.8-11A4.7 4.7 0 0 1 12 6a4.7 4.7 0 0 1 8.8 2.7Z" /></svg></button></div>
      <div className="restaurant-details"><div className={'restaurant-avatar avatar-' + restaurant.color}>{restaurant.icon}</div><div className="restaurant-info"><strong>{restaurant.name}</strong><span>{restaurant.type}</span><div className="restaurant-meta"><span>◷ &nbsp;{restaurant.time}</span><span>🛵 &nbsp;{restaurant.price}</span></div></div><span className="rating">★ <i>{restaurant.rating}</i></span></div>
      <div className="restaurant-quick-add"><span><strong>{dish.name}</strong><small>{money(dish.price)}</small></span><button type="button" onClick={() => addToCart(restaurant)} aria-label={'Add ' + dish.name + ' to cart'}>＋ Add</button></div>
    </article>
  }
  async function copyOfferCode(code) {
    try { await navigator.clipboard.writeText(code); setOfferNotice(code + ' copied to clipboard.') }
    catch { setOfferNotice('Copy this code at checkout: ' + code) }
  }
  function changeQuantity(id, amount) {
    setCart((items) => items.map((item) => item.id === id ? { ...item, quantity: item.quantity + amount } : item).filter((item) => item.quantity > 0))
  }
  function updateProfile(nextProfile) {
    setProfileData(nextProfile)
    localStorage.setItem('foodie.customer.profile', JSON.stringify(nextProfile))
    const nextIdentifier = nextProfile.email || nextProfile.phone || customer.identifier
    const nextFavoriteKey = nextIdentifier ? 'foodie.customer.favorites.' + encodeURIComponent(String(nextIdentifier).trim().toLowerCase()) : ''
    if (favoriteStorageKey && nextFavoriteKey && favoriteStorageKey !== nextFavoriteKey) {
      const existingFavorites = localStorage.getItem(favoriteStorageKey) || JSON.stringify(favorites)
      localStorage.setItem(nextFavoriteKey, existingFavorites)
    }
    const updatedCustomer = { ...customer, displayName: nextProfile.name || customer.displayName, identifier: nextIdentifier }
    setCustomer(updatedCustomer)
    sessionStorage.setItem('foodie.customer', JSON.stringify(updatedCustomer))
  }
  function openAccount(section) {
    setAccountSection(section)
    setProfileOpen(false)
  }
  function placeOrder() {
    if (!cart.length) return
    const additionalCharges = 150
    const order = {
      id: 'FD-' + String(orders.length + 1).padStart(6, '0'),
      createdAt: new Date().toISOString(),
      items: cart.map((item) => ({ ...item })),
      subtotal,
      additionalCharges,
      netTotal: subtotal + additionalCharges,
      points: rewardPoints(subtotal),
    }
    setOrders((current) => [...current, order])
    setCart([])

    setOrderConfirmation(order)
  }
  function signOut() {
    sessionStorage.removeItem('foodie.customer')
    setCustomer(null)
    setFavorites([])
    setProfileOpen(false)
    window.history.replaceState({}, '', '/home?mode=guest')
  }

  return <main className={'food-home' + (theme === 'dark' ? ' theme-dark' : '')}>
    <header className="food-nav">
      <Brand />
      <nav aria-label="Main navigation"><a className="selected" href="#home">Home</a><a href="#restaurants">Restaurants</a><a href="#restaurants">Track Order</a><a href="#offers">Offers</a></nav>
      <div className="nav-tools">
        <div className="search-autocomplete" ref={searchRef}>
          <div className="search-box"><svg className="search-lens" viewBox="0 0 24 24" aria-hidden="true"><circle cx="10.8" cy="10.8" r="6.5"/><path d="m16 16 4.5 4.5"/></svg><input value={search} onFocus={() => setSearchOpen(true)} onChange={(event) => { setSearch(event.target.value); setSearchOpen(true); setActiveSuggestion(0) }} onKeyDown={(event) => { if (event.key === 'ArrowDown' && searchSuggestions.length) { event.preventDefault(); setActiveSuggestion((index) => (index + 1) % searchSuggestions.length) } else if (event.key === 'ArrowUp' && searchSuggestions.length) { event.preventDefault(); setActiveSuggestion((index) => (index + searchSuggestions.length - 1) % searchSuggestions.length) } else if (event.key === 'Enter' && searchSuggestions.length) { event.preventDefault(); selectSearchResult(searchSuggestions[activeSuggestion]) } else if (event.key === 'Escape') setSearchOpen(false) }} placeholder="Search restaurants, dishes..." aria-label="Search restaurants and dishes" aria-expanded={searchOpen && Boolean(searchTerm)} aria-controls="restaurant-search-suggestions" aria-autocomplete="list" />{search && <button type="button" className="search-clear" aria-label="Clear search" onClick={() => { setSearch(''); setActiveSuggestion(0) }}>×</button>}</div>
          {searchOpen && searchTerm && <div className="search-suggestions" id="restaurant-search-suggestions" role="listbox"><small>RESTAURANTS & DISHES</small>{searchSuggestions.length ? searchSuggestions.map((restaurant, index) => <button type="button" role="option" aria-selected={activeSuggestion === index} id={'restaurant-suggestion-' + index} key={restaurant.name} onMouseDown={(event) => event.preventDefault()} onClick={() => selectSearchResult(restaurant)}><span className="suggestion-icon">{restaurant.icon}</span><span><strong>{restaurant.name}</strong><small>{restaurant.type} · {featuredDishes[restaurant.name].name}</small></span><b>↗</b></button>) : <p>No restaurant matches “{search}”. Try another name or dish.</p>}</div>}
        </div>
        <button className="basket" type="button" aria-label={'Shopping cart, ' + itemCount + ' items'} onClick={() => { setCartOpen(true); setProfileOpen(false) }}>🛒{itemCount > 0 && <i>{itemCount}</i>}</button>
        {isGuest
          ? <a className="profile profile-guest" href="/login" aria-label="Sign in to your customer account"><span aria-hidden="true">●</span><b>Sign in</b></a>
          : <div className="profile-control">
            <button className="profile" type="button" aria-label="Open your account" aria-expanded={profileOpen} onClick={() => setProfileOpen((open) => !open)}>{profileData.photo ? <img src={profileData.photo} alt="" /> : customerName.slice(0, 1).toUpperCase()}</button>
            {profileOpen && <div className="profile-menu">
              <div className="profile-menu-person"><span>{profileData.photo ? <img src={profileData.photo} alt="" /> : customerName.slice(0, 1).toUpperCase()}</span><div><strong>{customerName}</strong><small>{profileData.email || profileData.phone || customer.identifier}</small></div></div>
              <button type="button" onClick={() => openAccount('settings')}>Account settings</button>
              <button type="button" onClick={() => openAccount('orders')}>My orders</button>
              <button type="button" onClick={() => openAccount('wallet')}>Wallet</button>
              <button type="button" onClick={() => openAccount('rewards')}>Rewards</button>
              <button type="button" onClick={signOut}>Sign out</button>
            </div>}
          </div>}
      </div>
    </header>
    <section className="food-hero" id="home" aria-label={'Featured promotion ' + (activeSlide + 1) + ' of ' + slides.length} style={{ '--banner-background': slide.background }}>
      <img className="hero-banner-image" key={slide.src} src={slide.src} alt={slide.alt} draggable="false" fetchPriority={activeSlide === 0 ? 'high' : 'auto'} />
      <div className="hero-copy" key={'copy-' + slide.src}>
        <span className="home-eyebrow"><i />{slide.kicker}</span>
        <h1>{slide.titleFirst}<br /><em>{slide.titleAccent}</em></h1>
        <p>{slide.copy}</p>
        <div className="hero-actions"><a className="primary-cta" href={slide.primaryHref}>{slide.primaryLabel}<b>→</b></a><a className="secondary-cta" href={slide.secondaryHref}>{slide.secondaryLabel}</a></div>
      </div>
      <button className="slide-arrow slide-prev" aria-label="Previous banner" onClick={() => setActiveSlide((activeSlide + slides.length - 1) % slides.length)}>‹</button>
      <button className="slide-arrow slide-next" aria-label="Next banner" onClick={() => setActiveSlide((activeSlide + 1) % slides.length)}>›</button>
      <div className="slide-dots" aria-label="Choose banner">{slides.map((item, index) => <button key={item.src} aria-label={'Show banner ' + (index + 1)} aria-current={activeSlide === index ? 'true' : undefined} className={activeSlide === index ? 'current' : ''} onClick={() => setActiveSlide(index)} />)}</div>
    </section>
    <div className="home-content"><section className="browse-row" id="categories"><div className="categories-area"><h2>Explore by Category</h2><div className="category-list">{categories.map(([name, icon, color]) => <a className={'category-card ' + color} key={name} href="#restaurants"><span>{icon}</span><strong>{name}</strong></a>)}</div></div><aside className="offer-card" id="offers"><div><span>A little treat for</span><strong>your next order</strong><p>Get special offers and discounts<br />on your favourite meals.</p><button type="button" onClick={() => { setOffersOpen(true); setOfferNotice('') }}>View Offers <b>→</b></button></div><span className="offer-art" aria-hidden="true">🍕</span><i className="offer-spark">✦</i></aside></section>
      {!isGuest && favoriteRestaurants.length > 0 && <section className="favorites-section" aria-labelledby="favorites-title"><div className="popular-heading"><div><h2 id="favorites-title">Your favourites <span>♥</span></h2><p>Your saved restaurants are ready for your next visit.</p></div></div><div className="restaurant-list">{favoriteRestaurants.map(renderRestaurantCard)}</div></section>}
      <section className="popular" id="restaurants"><div className="popular-heading"><div><h2>Popular Near You <span>⌁</span></h2><p>Top-rated restaurants delivering great food in your area.</p></div><button type="button" className="view-all-restaurants" aria-expanded={showAllRestaurants} onClick={() => { setShowAllRestaurants((current) => !current); if (!showAllRestaurants) window.setTimeout(() => document.getElementById('restaurants')?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 0) }}>{showAllRestaurants ? 'Show popular restaurants' : 'View all restaurants'} <b>{showAllRestaurants ? '↑' : '→'}</b></button></div><div className="restaurant-list">{shownRestaurants.map(renderRestaurantCard)}</div>{shownRestaurants.length === 0 && <p className="empty-search">No restaurants found. Try another search.</p>}</section>
    </div>
    {offersOpen && <div className="feature-modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) setOffersOpen(false) }}><section className="offers-dialog" role="dialog" aria-modal="true" aria-labelledby="offers-title"><header><div><span>FOODIE DEALS</span><h2 id="offers-title">Offers for your next order</h2><p>Browse the current sample offers and their codes.</p></div><button type="button" aria-label="Close offers" onClick={() => setOffersOpen(false)}>×</button></header><div className="offers-list">{availableOffers.map((offer) => <article className="offer-detail-card" key={offer.code}><span className="offer-detail-icon">{offer.icon}</span><div><h3>{offer.title}</h3><p>{offer.detail}</p><div className="offer-code"><b>{offer.code}</b><button type="button" onClick={() => copyOfferCode(offer.code)}>Copy code</button></div></div></article>)}</div><small className="offers-demo-note">Sample offers for the customer portal preview. Offer redemption will connect to checkout.</small>{offerNotice && <p className="offers-notice" role="status">{offerNotice}</p>}</section></div>}
    {favoritePrompt && <div className="feature-modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) setFavoritePrompt(false) }}><section className="favorite-signin-dialog" role="dialog" aria-modal="true" aria-labelledby="favorite-signin-title"><button type="button" className="favorite-dialog-close" aria-label="Close" onClick={() => setFavoritePrompt(false)}>×</button><span className="favorite-dialog-heart">♡</span><h2 id="favorite-signin-title">Save your favourites</h2><p>Sign in to a customer account to save restaurants and see them here next time.</p><a href="/login">Sign in to continue <b>→</b></a><button type="button" className="favorite-keep-browsing" onClick={() => setFavoritePrompt(false)}>Keep browsing</button></section></div>}
    {cartOpen && <div className="cart-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) { setCartOpen(false); setOrderConfirmation(null) } }}><aside className="cart-drawer" role="dialog" aria-modal="true" aria-labelledby="cart-title">
      <header className="cart-header"><div><span className="cart-kicker">FOODIE CART</span><h2 id="cart-title">{orderConfirmation ? 'Order placed' : 'Your order'} <small>({itemCount})</small></h2></div><button type="button" className="cart-close" aria-label="Close cart" onClick={() => { setCartOpen(false); setOrderConfirmation(null) }}>×</button></header>
      {orderConfirmation ? <div className="order-confirmation"><span>✓</span><h3>Thanks for your order</h3><p>Order ID <b>{orderConfirmation.id}</b></p><p>You earned <b>★ {orderConfirmation.points} stars</b>.</p><small>This demo order is saved on this device. It has not been sent to the merchant.</small><button type="button" onClick={() => { setOrderConfirmation(null); setCartOpen(false) }}>Continue browsing</button></div>
      : cart.length === 0 ? <div className="cart-empty"><span>🛍️</span><h3>Your cart is empty</h3><p>Add a dish from one of the restaurants to get started.</p><button type="button" onClick={() => setCartOpen(false)}>Explore restaurants</button></div> : <>
        <div className="cart-lines">{cart.map((item) => <article className="cart-line" key={item.id}><div className="cart-food-icon">{featuredDishes[item.restaurant] ? featuredDishes[item.restaurant].icon : '🍽️'}</div><div className="cart-line-info"><strong>{item.name}</strong><small>{item.restaurant}</small><b>{money(item.price * item.quantity)}</b><div className="cart-quantity"><button type="button" aria-label={'Remove one ' + item.name} onClick={() => changeQuantity(item.id, -1)}>−</button><span>{item.quantity}</span><button type="button" aria-label={'Add one ' + item.name} onClick={() => changeQuantity(item.id, 1)}>＋</button></div></div><button type="button" className="cart-remove" aria-label={'Remove ' + item.name} onClick={() => setCart((items) => items.filter((entry) => entry.id !== item.id))}>×</button></article>)}</div>
        <div className="cart-summary"><div><span>Subtotal</span><b>{money(subtotal)}</b></div><div><span>Additional charges</span><b>{money(150)}</b></div><div className="cart-total"><strong>Net total</strong><strong>{money(subtotal + 150)}</strong></div><button type="button" className="cart-checkout" onClick={placeOrder}>Place demo order <span>→</span></button><p className="checkout-note">Demo order only: saved on this device, not sent to a merchant or payment service.</p></div>
      </>}
    </aside></div>}
    {accountSection && <AccountCenter section={accountSection} customer={customer} profile={profileData} onProfileChange={updateProfile} orders={orders} theme={theme} onThemeChange={setTheme} onClose={() => setAccountSection('')} />}
  </main>
}