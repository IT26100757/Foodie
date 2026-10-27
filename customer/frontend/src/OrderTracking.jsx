import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import './OrderTracking.css'
import './OrderTrackingProvider.css'
import { confirmDelivery, mountTrackingMap, saveOrderUpdate, subscribeToOrder } from './orderTrackingService.js'

const steps = ['Order Placed', 'Preparing', 'Picked Up', 'Out for Delivery', 'Delivered']
const demoDriver = { name: 'Kasun Perera', rating: '4.8 (320 deliveries)', phone: '+94771234567', photo: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=120&q=80', vehicleNumber: 'WP BX 3256', vehicleType: 'Honda Dio – Red' }
const demoStages = ['Order Placed', 'Preparing', 'Picked Up', 'Out for Delivery']

function normalizeStage(order) {
  if (order.deliveryStatus === 'Delivered' || order.status === 'Delivered') return 'Delivered'
  if (order.deliveryStatus === 'Arrived') return 'Arrived'
  return demoStages.includes(order.deliveryStatus) ? order.deliveryStatus : 'Order Placed'
}

function formatMoney(amount) {
  return `Rs. ${Number(amount || 0).toLocaleString('en-LK')}`
}

function formatPlacedAt(value) {
  const date = value ? new Date(value) : new Date()
  return Number.isNaN(date.getTime()) ? 'Order time unavailable' : date.toLocaleString('en-LK', { day: 'numeric', month: 'long', year: 'numeric', hour: 'numeric', minute: '2-digit' })
}

function addressParts(address = {}) {
  return [address.address1, address.address2, address.address3, address.city, address.postalCode].filter((part) => String(part || '').trim())
}

function DeliveryAddress({ order }) {
  const address = order?.deliveryAddress || order?.address || {}
  const parts = addressParts(address)
  return <section className="tracking-delivery-address" aria-label="Delivery address"><span>DELIVERY ADDRESS</span><strong>{order?.customer?.name || 'Customer'}</strong><p>{parts.length ? parts.join(', ') : 'Address not provided'}</p>{order?.customer?.phone && <small>☎ {order.customer.phone}</small>}</section>
}

function OrderMap({ order, stage, live, onLocation }) {
  const mapElement = useRef(null)
  const approaching = stage === 'Out for Delivery' || stage === 'Arrived'
  const address = order?.deliveryAddress || order?.address || {}
  const destination = address.city || address.address3 || address.address1 || 'Delivery address'
  useEffect(() => {
    if (!mapElement.current) return undefined
    const mapContainer = mapElement.current.parentElement
    const cleanup = mountTrackingMap(mapElement.current, order, onLocation, () => mapContainer?.classList.remove('provider-map-mounted'))
    if (cleanup) mapContainer?.classList.add('provider-map-mounted')
    return typeof cleanup === 'function' ? cleanup : undefined
  }, [order, onLocation])
  return <div className={`tracking-map${approaching ? ' driver-near' : ''}`} role="img" aria-label="Illustrated demo map unless a configured map provider supplies live map data">
    <div className="provider-map-layer" ref={mapElement} />
    <svg className="map-art" viewBox="0 0 900 510" preserveAspectRatio="xMidYMid slice" aria-hidden="true">
      <rect width="900" height="510" fill="#e9f0ed" />
      <path d="M0 86 249 28l184 73 172-58 295 71v158l-227 35-174 140-260-54L0 464z" fill="#dbead7" />
      <path d="M0 182 153 139l84 65-41 74L0 316zm610-153 57 54-36 93-82-17-27-66zm125 289 165-62v149l-195 77-67-82z" fill="#d2e8cb" />
      <g fill="none" stroke="#fffdf6" strokeWidth="25" strokeLinecap="round"><path d="M-20 408 230 333l134-70 132 22 143-107 290-67"/><path d="m105-30 76 132 124 94 35 169 149 174"/><path d="m439-30-13 115 58 104-31 117 49 149"/><path d="m718-30-70 124 43 104-76 129 62 215"/><path d="M-20 128 171 96l191 49 197-29 170 54 200-42"/></g>
      <g fill="none" stroke="#d5cdbd" strokeWidth="3"><path d="M-20 408 230 333l134-70 132 22 143-107 290-67"/><path d="m105-30 76 132 124 94 35 169 149 174"/><path d="m439-30-13 115 58 104-31 117 49 149"/><path d="m718-30-70 124 43 104-76 129 62 215"/><path d="M-20 128 171 96l191 49 197-29 170 54 200-42"/></g>
      <path className="route-glow" d={approaching ? 'M240 352 C315 330 345 280 418 277 S538 302 610 220' : 'M220 340 C315 325 325 260 420 270 S530 323 610 220'} />
      <path className="route-line" d={approaching ? 'M240 352 C315 330 345 280 418 277 S538 302 610 220' : 'M220 340 C315 325 325 260 420 270 S530 323 610 220'} />
      <text x="115" y="227" className="map-label">Narahenpita</text><text x="515" y="106" className="map-label">Viharamahadevi Park</text><text x="695" y="315" className="map-label">Colombo 07</text>
      <g className="restaurant-pin" transform="translate(220 340)"><circle r="20"/><text y="7" textAnchor="middle">⌂</text></g>
      <g className="destination-pin" transform="translate(610 220)"><circle r="19"/><text y="7" textAnchor="middle">⌖</text></g>
      <g className="driver-pin" transform={approaching ? 'translate(535 270)' : 'translate(420 270)'}><circle className="driver-halo" r="37"/><circle className="driver-disc" r="25"/><text y="8" textAnchor="middle">🛵</text></g>
    </svg>
    <div className="map-chip restaurant-chip"><span>🍕</span><b>{order.restaurant || order.items?.[0]?.restaurant || 'Restaurant'}</b></div>
    <div className="map-chip driver-chip"><span className="map-pin-dot" /><span><b>{approaching ? 'Driver is nearby' : 'Driver is on the way'}</b><small>{approaching ? '1 min away' : '3–5 min away'}</small></span></div>
    <div className="map-chip address-chip"><span>⌂</span><span><b>Your Location</b><small>{destination}</small></span></div>
    <span className="map-demo-label">{live ? 'LIVE LOCATION' : 'DEMO MAP · SAMPLE ROUTE'}</span>
  </div>
}

function ProgressTimeline({ stage }) {
  const active = stage === 'Arrived' ? 3 : Math.max(0, steps.indexOf(stage))
  return <ol className="tracking-timeline" aria-label="Order progress">{steps.map((label, index) => {
    const complete = stage === 'Delivered' || index < active
    const current = stage !== 'Delivered' && index === active
    return <li className={`${complete ? 'complete' : ''}${current ? ' current' : ''}`} key={label} aria-current={current ? 'step' : undefined}>
      <span className="timeline-dot">{complete ? '✓' : index === 2 || index === 3 ? '🛵' : index === 4 ? '⌂' : ''}</span>
      <b>{label}</b><small>{index === 0 ? '2:30 PM' : index === 1 ? '2:40 PM' : index === 2 ? '2:50 PM' : index === 3 ? 'Now' : '—:—'}</small>
    </li>
  })}</ol>
}

function DriverCard({ driver, demo }) {
  return <section className="driver-details" aria-label="Delivery driver">
    <div className="driver-person"><img src={driver.photo || demoDriver.photo} alt="" /><div><h3>{driver.name || 'Your delivery driver'}</h3><p><span>★</span> {driver.rating || 'Driver rating unavailable'}{demo && <small> · sample driver</small>}</p></div></div>
    <div className="driver-actions"><a href={`tel:${driver.phone || demoDriver.phone}`} aria-label={`Call ${driver.name || 'driver'}`} title="Call driver"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6.6 3.8 9 3.3l2.1 4.9-2 1.7a15.5 15.5 0 0 0 5 5l1.7-2 4.9 2.1-.5 2.4a3 3 0 0 1-3.4 2.3A17.8 17.8 0 0 1 4.3 7.2a3 3 0 0 1 2.3-3.4Z" /></svg></a><a href={`sms:${driver.phone || demoDriver.phone}`} aria-label={`Message ${driver.name || 'driver'}`} title="Message driver"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 5h16v12h-9l-5 3v-3H4z"/><path d="M8 10h8m-8 3h5" /></svg></a></div>
    <div className="vehicle-facts"><div><span>🛵</span><p>Vehicle Number<strong>{driver.vehicleNumber || 'Not provided'}</strong></p></div><div><span>🛵</span><p>Vehicle Type<strong>{driver.vehicleType || 'Not provided'}</strong></p></div></div>
  </section>
}

export default function OrderTracking({ orders, onOrderChange }) {
  const [selectedId, setSelectedId] = useState(orders.find((order) => normalizeStage(order) !== 'Delivered')?.id || orders[0]?.id || '')
  const [liveLocation, setLiveLocation] = useState(null)
  const [connectedOrderId, setConnectedOrderId] = useState('')
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState('')
  const [issueOpen, setIssueOpen] = useState(false)
  const [issueText, setIssueText] = useState('')
  const order = useMemo(() => orders.find((item) => item.id === selectedId) || orders[0], [orders, selectedId])
  const stage = order ? normalizeStage(order) : 'Order Placed'
  const driver = order?.driver || demoDriver
  const demo = !order?.driver
  const providerConnected = connectedOrderId === order?.id
  const hasLiveLocation = liveLocation?.orderId === order?.id
  const handleLocation = useCallback((location) => {
    setLiveLocation({ ...location, orderId: order?.id })
    setConnectedOrderId(order?.id || '')
  }, [order?.id])
  const arrived = stage === 'Arrived'
  const delivered = stage === 'Delivered'

  useEffect(() => {
    if (!order || delivered) return undefined
    return subscribeToOrder(order.id, (update) => {
      setConnectedOrderId(order.id)
      if (update?.latitude != null && update?.longitude != null) setLiveLocation({ ...update, orderId: order.id })
      if (update?.status && (demoStages.includes(update.status) || update.status === 'Arrived' || update.status === 'Delivered')) {
        onOrderChange({ ...order, ...update, deliveryStatus: update.status })
      }
    }, () => setConnectedOrderId(''))
  }, [order, delivered, onOrderChange])

  async function updateStage(nextStage) {
    if (!order) return
    const updated = { ...order, deliveryStatus: nextStage }
    onOrderChange(updated)
    setNotice('')
    try {
      const result = await saveOrderUpdate(updated)
      if (result.connected) setConnectedOrderId(order.id)
      if (!result.connected) setNotice('Demo tracking · this update is saved on this device only.')
    } catch {
      setNotice('The tracking service could not be reached. This update is saved on this device only.')
    }
  }

  async function receiveOrder() {
    if (!order || busy) return
    setBusy(true)
    const updated = { ...order, deliveryStatus: 'Delivered', deliveredAt: new Date().toISOString() }
    try {
      const result = await confirmDelivery(updated)
      onOrderChange(updated)
      if (result.connected) setConnectedOrderId(order.id)
      setNotice(result.connected
        ? 'Delivery confirmed. The connected service has been asked to notify the merchant and driver.'
        : 'Demo confirmation · order marked delivered on this device. Merchant and driver notifications are not connected.')
    } catch {
      onOrderChange(updated)
      setNotice('Delivery was marked on this device, but merchant and driver notifications could not be sent.')
    } finally {
      setBusy(false)
    }
  }

  function submitIssue(event) {
    event.preventDefault()
    if (!issueText.trim()) return
    setNotice('Issue saved on this device. Customer support messaging is not connected yet.')
    setIssueText('')
    setIssueOpen(false)
  }

  if (!order) return <section className="standalone-page tracking-page"><div className="tracking-heading"><span>ORDER UPDATES</span><h1>Track <em>your order.</em></h1><p>Live delivery updates, all in one place.</p></div><div className="tracking-empty"><span>🛵</span><h2>No orders to track yet</h2><p>Once you place an order, its delivery updates will appear here.</p><a className="primary-cta" href="/restaurants">Explore restaurants <b>→</b></a></div></section>

  const currentStep = stage === 'Arrived' ? 'Out for Delivery' : stage
  const stepIndex = steps.indexOf(currentStep)
  const nextStage = demoStages[Math.min(Math.max(stepIndex + 1, 1), demoStages.length - 1)]

  return <section className="standalone-page tracking-page">
    {orders.length > 1 && <label className="tracking-order-select">Choose order<select value={order.id} onChange={(event) => { setSelectedId(event.target.value); setNotice('') }}>{orders.map((item) => <option value={item.id} key={item.id}>{item.id} · {item.restaurant || item.items?.[0]?.restaurant || 'Foodie'}</option>)}</select></label>}
    <div className="tracking-heading-row"><a className="tracking-back" href="/home" aria-label="Back to home">‹</a><div className="tracking-heading"><span>{delivered ? 'DELIVERY COMPLETE' : arrived ? 'DRIVER ARRIVED' : 'ORDER UPDATES'}</span><h1>{delivered ? <>Order <em>Delivered</em></> : arrived ? <>Your order <em>has arrived!</em></> : <>Track <em>Your Order</em></>}</h1><p>{delivered ? 'Your food has arrived!' : arrived ? 'Please confirm once you have received your order.' : stage === 'Out for Delivery' ? 'Your order is almost there!' : 'Live updates on your delicious food.'}</p></div></div>
    {delivered ? <div className="delivered-layout"><article className="delivered-card"><span className="delivered-check">✓</span><h2>Thank You!</h2><p>Your order has been marked as delivered.</p><strong>Enjoy your meal! 🍔</strong><a className="tracking-primary" href="/home">Back to Home</a><small>{notice}</small></article><div className="delivered-summary"><div className="tracking-order-card"><RestaurantSummary order={order} /><OrderSummary order={order} /><DeliveryAddress order={order} /><ProgressTimeline stage="Delivered" /></div><div className="notification-preview"><span>🔔</span><div><small>DEMO NOTIFICATION STATUS</small><p>{notice || 'Local order status updated.'}</p></div></div></div></div>
      : <div className={`tracking-layout${arrived ? ' is-arrived' : ''}`}>
        <article className="tracking-order-card">
          <RestaurantSummary order={order} />
          <OrderSummary order={order} />
          <DeliveryAddress order={order} />
          <ProgressTimeline stage={stage} />
          {!delivered && <OrderMap order={order} stage={stage} live={providerConnected && hasLiveLocation} onLocation={handleLocation} />}
          {arrived && <div className="arrival-art"><span>🛍️</span><h2>Your Order has Arrived!</h2><p>Please confirm once you have received your order.</p></div>}
          <DriverCard driver={driver} demo={demo} />
          <div className="delivery-meta"><div><span>◷</span><p>Estimated Arrival<strong>{stage === 'Preparing' ? '20–25 min' : stage === 'Picked Up' ? '8–12 min' : stage === 'Out for Delivery' ? '1–2 min' : '25–35 min'}</strong></p></div><div><span>⌖</span><p>Location status<strong>{providerConnected && liveLocation ? 'Live location connected' : 'Demo location · not GPS'}</strong></p></div></div>
          {notice && <p className="tracking-notice" role="status">{notice}</p>}
          {arrived ? <div className="arrival-actions"><button className="tracking-primary" type="button" onClick={receiveOrder} disabled={busy}>{busy ? 'Confirming…' : 'I have received my order'}</button><button className="tracking-secondary" type="button" onClick={() => setIssueOpen((open) => !open)}>Report an issue</button>{issueOpen && <form className="issue-form" onSubmit={submitIssue}><label htmlFor="tracking-issue">What went wrong?</label><textarea id="tracking-issue" value={issueText} onChange={(event) => setIssueText(event.target.value)} required maxLength={500} placeholder="Tell us what happened" /><button className="tracking-primary" type="submit">Save issue</button></form>}</div>
            : <div className="demo-step-actions"><span>{providerConnected ? 'Connected to order updates' : 'Demo mode · sample driver and route'}</span>{stage === 'Out for Delivery' ? <button type="button" onClick={() => updateStage('Arrived')}>Show driver arrived (demo) <b>→</b></button> : <button type="button" onClick={() => updateStage(nextStage)}>Advance demo tracking <b>→</b></button>}</div>}
          {notice && !arrived && <p className="tracking-notice" role="status">{notice}</p>}
        </article>
      </div>}
  </section>
}

function RestaurantSummary({ order }) {
  const restaurant = order.restaurant || order.items?.[0]?.restaurant || 'Foodie restaurant'
  return <div className="tracking-restaurant"><div className="restaurant-food-thumb">🍕</div><div><h2>{restaurant}</h2><p>{order.items?.[0]?.category || 'Pizza · Italian'}</p></div><a href="/restaurants">View Restaurant</a></div>
}

function OrderSummary({ order }) {
  return <div className="tracking-summary"><div><strong>Order #{order.id}</strong><span>Placed on {formatPlacedAt(order.createdAt)}</span></div><div><b>Total</b><strong>{formatMoney(order.netTotal)}</strong></div></div>
}
