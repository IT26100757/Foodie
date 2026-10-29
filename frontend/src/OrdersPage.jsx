import { useEffect, useMemo, useRef, useState } from 'react'

const icons = {
  orders: <><path d="M6 3h9l4 4v14H6z" /><path d="M14 3v5h5M9 12h7M9 16h7" /></>,
  calendar: <><rect x="3" y="5" width="18" height="16" rx="2" /><path d="M16 3v4M8 3v4M3 10h18" /></>,
  chevronDown: <path d="m6 9 6 6 6-6" />,
  previous: <path d="m15 18-6-6 6-6" />,
  next: <path d="m9 18 6-6-6-6" />,
}

function Icon({ name, size = 18, className = '' }) {
  return <svg className={`ui-icon ${className}`} width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{icons[name]}</svg>
}

function dateKey(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export default function OrdersPage({
                                     orders,
                                     restaurantId,
                                     onStatusChange,
                                     updatingOrderIds = [],
                                     orderActionError = ''
                                   }) {
  const [filter, setFilter] = useState('All orders')
  const [sortOrder, setSortOrder] = useState('newest')
    const [sortOpen, setSortOpen] = useState(false)
  const [currentPage, setCurrentPage] = useState(1)
  const pageSize = 10

  const statusFilters = [
    'All orders',
    'New',
    'Preparing',
    'Ready',
    'On delivery',
    'Delivered',
    'Cancelled'
  ]
  const [query, setQuery] = useState('')
  const [date, setDate] = useState('')
  const [calendarOpen, setCalendarOpen] = useState(false)
  const [visibleMonth, setVisibleMonth] = useState(() => new Date(new Date().getFullYear(), new Date().getMonth(), 1))
  const [expanded, setExpanded] = useState('')
  const calendarRef = useRef(null)

  const [orderItems, setOrderItems] = useState({})
  const [itemsLoading, setItemsLoading] = useState({})
  const [itemsErrors, setItemsErrors] = useState({})

  useEffect(() => {
    if (!expanded || !restaurantId) return

    const orderId = expanded
    const cacheKey = `${restaurantId}:${orderId}`

    if (Object.prototype.hasOwnProperty.call(orderItems, cacheKey)) return

    const controller = new AbortController()

    async function loadItems() {
      setItemsLoading((prev) => ({ ...prev, [cacheKey]: true }))
      setItemsErrors((prev) => ({ ...prev, [cacheKey]: '' }))

      try {
        const response = await fetch(
            `/api/merchant/restaurants/${restaurantId}/orders/${orderId}/items`,
            {
              credentials: 'include',
              signal: controller.signal
            }
        )

        if (!response.ok) {
          throw new Error(`Failed to load order items (${response.status})`)
        }

        const items = await response.json()

        if (!Array.isArray(items)) {
          throw new Error('Unexpected order items response')
        }

        setOrderItems((prev) => ({ ...prev, [cacheKey]: items }))
      } catch (error) {
        if (error.name !== 'AbortError') {
          setItemsErrors((prev) => ({
            ...prev,
            [cacheKey]: error.message
          }))
        }
      } finally {
        if (!controller.signal.aborted) {
          setItemsLoading((prev) => ({ ...prev, [cacheKey]: false }))
        }
      }
    }

    loadItems()

    return () => controller.abort()
  }, [expanded, restaurantId, orderItems])


  useEffect(() => {
    if (!calendarOpen) return
    function closeOutside(event) {
      if (!calendarRef.current?.contains(event.target)) setCalendarOpen(false)
    }
    function closeOnEscape(event) {
      if (event.key === 'Escape') setCalendarOpen(false)
    }
    document.addEventListener('pointerdown', closeOutside)
    document.addEventListener('keydown', closeOnEscape)
    return () => {
      document.removeEventListener('pointerdown', closeOutside)
      document.removeEventListener('keydown', closeOnEscape)
    }
  }, [calendarOpen])

  const calendarDays = useMemo(() => {
    const first = new Date(visibleMonth.getFullYear(), visibleMonth.getMonth(), 1)
    const offset = first.getDay()
    return Array.from({ length: 42 }, (_, index) => new Date(visibleMonth.getFullYear(), visibleMonth.getMonth(), index - offset + 1))
  }, [visibleMonth])


  const filteredOrders = [...orders]
      .filter((order) => {
        const status = String(order.status || '').toUpperCase()

        const matchesFilter =
            filter === 'All orders' ||
            status === filter.toUpperCase().replaceAll(' ', '_')

        const searchText = query.trim().toLowerCase()

        const matchesQuery =
            !searchText ||
            String(order.id).toLowerCase().includes(searchText) ||
            String(order.customerName || '')
                .toLowerCase()
                .includes(searchText)

        const matchesDate =
            !date ||
            String(order.createdAt || order.date || '')
                .slice(0, 10) === date

        return matchesFilter && matchesQuery && matchesDate
      })
      .sort((a, b) => {
        const aTime = Date.parse(a.createdAt || a.date || '')
        const bTime = Date.parse(b.createdAt || b.date || '')

        const aValue = Number.isFinite(aTime)
            ? aTime
            : Number(a.id) || 0

        const bValue = Number.isFinite(bTime)
            ? bTime
            : Number(b.id) || 0

        return sortOrder === 'newest'
            ? bValue - aValue
            : aValue - bValue
      })

  const totalPages = Math.max(
      1,
      Math.ceil(filteredOrders.length / pageSize)
  )

  const safeCurrentPage = Math.min(currentPage, totalPages)

  const visibleOrders = filteredOrders.slice(
      (safeCurrentPage - 1) * pageSize,
      safeCurrentPage * pageSize
  )

  useEffect(() => {
    setCurrentPage(1)
  }, [filter, query, date, sortOrder])


  const formattedDate = date ? new Intl.DateTimeFormat('en-GB', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(new Date(`${date}T00:00:00`)) : 'Select date'

  return <div className="dashboard-content section-content orders-content">
    <div className="dashboard-heading-row"><div><span className="dashboard-kicker">ORDER MANAGEMENT</span><h1>Orders<span>.</span></h1><p>Follow new orders and keep every order moving.</p></div></div>
    <div className="order-toolbar"><div className="order-filters" role="group" aria-label="Filter orders">{statusFilters.map((item) => <button type="button" key={item} className={filter === item ? 'selected' : ''} onClick={() => setFilter(item)}>{item}</button>)}</div><div className="order-search"><label className="order-id-search"><span className="sr-only">Search by order ID</span><Icon name="orders" size={16} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search order ID or customer" /></label><div className="order-date-picker" ref={calendarRef}><button className={`order-date-trigger ${date ? 'has-date' : ''}`} type="button" aria-haspopup="dialog" aria-expanded={calendarOpen} onClick={() => setCalendarOpen((open) => !open)}><Icon name="calendar" size={17} /><span>{formattedDate}</span><Icon name="chevronDown" size={15} className="date-trigger-chevron" /></button>
      {calendarOpen && <section className="order-calendar" role="dialog" aria-label="Choose order date"><div className="calendar-topline"><div><span className="calendar-eyebrow">FILTER ORDERS</span><strong>{date ? new Intl.DateTimeFormat('en', { weekday: 'short', month: 'short', day: 'numeric' }).format(new Date(`${date}T00:00:00`)) : 'Select a date'}</strong></div></div><div className="calendar-month-row"><strong>{new Intl.DateTimeFormat('en', { month: 'long', year: 'numeric' }).format(visibleMonth)}</strong><div><button type="button" aria-label="Previous month" onClick={() => setVisibleMonth((month) => new Date(month.getFullYear(), month.getMonth() - 1, 1))}><Icon name="previous" size={17} /></button><button type="button" aria-label="Next month" onClick={() => setVisibleMonth((month) => new Date(month.getFullYear(), month.getMonth() + 1, 1))}><Icon name="next" size={17} /></button></div></div><div className="calendar-grid" role="grid"><div className="calendar-weekdays" role="row">{['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map((day) => <span role="columnheader" key={day}>{day}</span>)}</div><div className="calendar-dates" role="row">{calendarDays.map((day) => { const key = dateKey(day); const outsideMonth = day.getMonth() !== visibleMonth.getMonth(); const selected = date === key; const today = dateKey(new Date()) === key; return <button type="button" role="gridcell" key={key} className={`${outsideMonth ? 'outside-month' : ''} ${selected ? 'selected-day' : ''} ${today ? 'today' : ''}`} aria-label={new Intl.DateTimeFormat('en', { dateStyle: 'full' }).format(day)} aria-pressed={selected} onClick={() => { setDate(key); setCalendarOpen(false) }}>{day.getDate()}</button> })}</div></div><div className="calendar-footer"><button type="button" onClick={() => { setDate(''); setCalendarOpen(false) }}>Clear date</button><button type="button" onClick={() => { setDate(dateKey(new Date())); setVisibleMonth(new Date(new Date().getFullYear(), new Date().getMonth(), 1)); setCalendarOpen(false) }}>Today</button></div></section>}
      </div>
        <div className="order-sort-wrapper">
            <button
                type="button"
                className={`order-sort-trigger ${sortOpen ? 'is-open' : ''}`}
                onClick={() => setSortOpen((prev) => !prev)}
                aria-expanded={sortOpen}
                aria-haspopup="true"
            >
                <span>{sortOrder === 'newest' ? 'Newest first' : 'Oldest first'}</span>
                <Icon name="chevronDown" size={16} />
            </button>

            {sortOpen && (
                <div className="order-sort-menu">
                    {[
                        { value: 'newest', label: 'Newest first' },
                        { value: 'oldest', label: 'Oldest first' }
                    ].map((option) => (
                        <button
                            type="button"
                            key={option.value}
                            className={sortOrder === option.value ? 'selected' : ''}
                            onClick={() => {
                                setSortOrder(option.value)
                                setSortOpen(false)
                            }}
                        >
                            <span>{option.label}</span>
                            {sortOrder === option.value && <span>✓</span>}
                        </button>
                    ))}
                </div>
            )}
        </div>
    </div></div>
    {orderActionError && (
        <p role="alert" style={{ color: '#c0392b', marginBottom: 12 }}>
          {orderActionError}
        </p>
    )}
    <section className="dashboard-card orders-table-card" aria-label="Orders"><div className="orders-table-scroll"><table className="orders-table"><thead><tr><th>Order</th>
        <th>Date &amp; time</th>
        <th>Payment type</th>
        <th>Total</th>
        <th>Status</th>
        <th>Actions</th></tr></thead><tbody>
    {visibleOrders.map((order) => (
        <OrderRows
            key={order.id}
            order={order}
            expanded={expanded}
            setExpanded={setExpanded}
            onStatusChange={onStatusChange}
            items={orderItems[`${restaurantId}:${order.id}`]}
            itemsLoading={itemsLoading[`${restaurantId}:${order.id}`]}
            itemsError={itemsErrors[`${restaurantId}:${order.id}`]}
            isUpdating={updatingOrderIds.some(
                (id) => String(id) === String(order.id)
            )}
        />
    ))}
      {!filteredOrders.length && <tr><td colSpan="6"><div className="orders-empty"><span className="empty-state-mark"><Icon name="orders" size={20} /></span><strong>{orders.length ? 'No orders match these filters' : 'No orders yet'}</strong><p>{orders.length ? 'Try another order ID or date.' : 'New orders for your restaurant will appear here.'}</p></div></td></tr>}
    </tbody></table></div>
      <div className="orders-footnote"
           style={{
             display: 'flex',
             alignItems: 'center',
             justifyContent: 'space-between',
             gap: 12,
             flexWrap: 'wrap'
           }}
      >
  <span>
    Showing {filteredOrders.length === 0
      ? 0
      : (currentPage - 1) * pageSize + 1}
    {'–'}
    {Math.min(currentPage * pageSize, filteredOrders.length)}
    {' of '}
    {filteredOrders.length} orders
  </span>

        {totalPages > 1 && (
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <button
                  type="button"
                  disabled={currentPage === 1}
                  onClick={() => setCurrentPage((page) => page - 1)}
              >
                Previous
              </button>

              <span>Page {currentPage} of {totalPages}</span>

              <button
                  type="button"
                  disabled={currentPage === totalPages}
                  onClick={() => setCurrentPage((page) => page + 1)}
              >
                Next
              </button>
            </div>
        )}
      </div>
    </section>
  </div>
}

function readAmount(value) {
  if (value === null || value === undefined || value === '') return null
  const parsed = typeof value === 'number' ? value : Number(String(value).replace(/[^\d.-]/g, ''))
  return Number.isFinite(parsed) ? parsed : null
}

function formatAmount(value, currency = 'LKR') {
  const amount = readAmount(value)
  if (amount === null) return '—'
  try {
    return new Intl.NumberFormat('en-LK', { style: 'currency', currency, minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(amount)
  } catch {
    return `${currency} ${amount.toFixed(2)}`
  }
}

function OrderRows({
                     order,
                     expanded,
                     setExpanded,
                     onStatusChange,
                     isUpdating,
                     items,
                     itemsLoading,
                     itemsError
                   }) {
  const id = String(order.id)
    const [trackOpen, setTrackOpen] = useState(false)

    const trackingSteps = [
        { status: 'NEW', label: 'Order placed' },
        { status: 'PREPARING', label: 'Preparing food' },
        {
            status: 'READY',
            label: String(order.orderType || '').toUpperCase() === 'DELIVERY'
                ? 'Ready for rider pickup'
                : 'Ready for customer pickup'
        },
        { status: 'ON_DELIVERY', label: 'On delivery' },
        { status: 'DELIVERED', label: 'Delivered' }
    ]

    const currentStep = trackingSteps.findIndex(
        (step) => step.status === String(order.status || '').toUpperCase()
    )

    const total = readAmount(order.totalAmount ?? order.total)

  const currency = order.currency || 'LKR'

  const status = String(order.status || '').toUpperCase()

  const actions = {
    NEW: [
      { label: 'Accept Order', next: 'PREPARING' },
      { label: 'Reject Order', next: 'CANCELLED' }
    ],
    PREPARING: [
      { label: 'Mark Ready', next: 'READY' },
      { label: 'Cancel Order', next: 'CANCELLED' }
    ],
    READY: [
      { label: 'Cancel Order', next: 'CANCELLED' }
    ]
  }[status] || []

  function handleAction(action) {
    if (action.next === 'CANCELLED') {
      const confirmed = window.confirm(
          `Are you sure you want to ${action.label.toLowerCase()} #${order.id}?`
      )
      if (!confirmed) return
    }

    onStatusChange(order.id, action.next)
  }

  return <>
    <tr onClick={() => setExpanded((value) => value === id ? '' : id)} className="order-row"><td>
      <strong>#{order.id}</strong>
      <small style={{ display: 'block', marginTop: 5 }}>
        {order.customerName || 'Customer not provided'}
      </small>
    </td><td><span>
  {order.createdAt
    ? new Date(order.createdAt).toLocaleDateString('en-GB')
    : order.date || '—'}
</span>
<small>
  {order.createdAt
    ? new Date(order.createdAt).toLocaleTimeString('en-GB', {
        hour: '2-digit',
        minute: '2-digit'
      })
    : order.time || '—'}
</small></td><td>{order.paymentType || '—'}</td><td className="order-amount">{formatAmount(total, currency)}</td><td><span className={`order-status ${String(order.status || '').toLowerCase()}`}>{order.status || 'Pending'}</span></td>
        <td>
            <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: 12
            }}>
                <button
                    type="button"
                    onClick={(event) => {
                        event.stopPropagation()

                        if (expanded === id && trackOpen) {
                            setTrackOpen(false)
                        } else {
                            setExpanded(id)
                            setTrackOpen(true)
                        }
                    }}
                    style={{
                        background: '#153d30',
                        color: '#ffffff',
                        border: 'none',
                        borderRadius: 8,
                        padding: '8px 14px',
                        cursor: 'pointer',
                        fontWeight: 600
                    }}
                >
                    {trackOpen && expanded === id ? 'Hide' : 'Track'}
                </button>

                <Icon
                    name="chevronDown"
                    size={15}
                    className={expanded === id ? 'expanded' : ''}
                />
            </div>
        </td>
    </tr>

    {expanded === id && (
        <tr className="order-detail-row">
          <td colSpan="6">
            <div style={{ padding: '16px 20px' }}>
              <strong>Order #{order.id}</strong>
                {trackOpen && (
                    <div style={{
                        background: '#ffffff',
                        border: '1px solid #e5e0d9',
                        borderRadius: 14,
                        padding: 20,
                        marginTop: 16,
                        marginBottom: 20
                    }}>
                        <h3 style={{ marginTop: 0, color: '#153d30' }}>
                            Order Tracking
                        </h3>

                        {String(order.status).toUpperCase() === 'CANCELLED' ? (
                            <p style={{ color: '#c0392b', fontWeight: 600 }}>
                                This order has been cancelled.
                            </p>
                        ) : (
                            trackingSteps.map((step, index) => {
                                const completed = index < currentStep
                                const active = index === currentStep

                                return (
                                    <div
                                        key={step.status}
                                        style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: 12,
                                            padding: '10px 0',
                                            color: completed || active
                                                ? '#153d30'
                                                : '#9ca3af'
                                        }}
                                    >
            <span style={{
                width: 26,
                height: 26,
                borderRadius: '50%',
                background: completed || active
                    ? '#153d30'
                    : '#e5e7eb',
                color: completed || active
                    ? '#ffffff'
                    : '#6b7280',
                display: 'grid',
                placeItems: 'center',
                fontWeight: 700,
                flexShrink: 0
            }}>
              {completed ? '✓' : index + 1}
            </span>

                                        <span style={{
                                            fontWeight: active ? 700 : 500
                                        }}>
              {step.label}
                                            {active ? ' — Current status' : ''}
            </span>
                                    </div>
                                )
                            })
                        )}

                        <small style={{ color: '#6b7280' }}>
                            Tracking reflects the current order status.
                            Live rider location is not available yet.
                        </small>
                    </div>
                )}

              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
                gap: 16,
                marginTop: 16,
                marginBottom: 16
              }}>
                <div>
                  <strong>Customer</strong>
                  <p>{order.customerName || 'Not provided'}</p>
                </div>

                <div>
                  <strong>Phone</strong>
                  <p>{order.customerPhone || 'Not provided'}</p>
                </div>

                <div>
                  <strong>Order Type</strong>
                  <p>{order.orderType || 'Not specified'}</p>
                </div>

                <div>
                  <strong>Delivery Address</strong>
                  <p>{order.deliveryAddress || 'Not applicable'}</p>
                </div>
              </div>


              <p style={{ marginTop: 8 }}>
                Status: {order.status || 'Pending'}
              </p>


              <div style={{ marginTop: 12 }}>
                <strong>Ordered items</strong>

                {itemsLoading && <p>Loading items...</p>}

                {itemsError && (
                    <p role="alert" style={{ color: '#c0392b' }}>
                      {itemsError}
                    </p>
                )}

                {!itemsLoading && !itemsError && items?.length === 0 && (
                    <p>No items found.</p>
                )}

                {items?.map((item, index) => (
                    <p key={item.id ?? index}>
                      {item.itemName || item.name || 'Item'}
                      {' × '}
                      {item.quantity}
                      {' — '}
                      {formatAmount(item.subtotal, currency)}
                    </p>
                ))}
              </div>


              {actions.length > 0 && (
                  <div
                      style={{
                        display: 'flex',
                        gap: 10,
                        flexWrap: 'wrap',
                        marginTop: 16
                      }}
                  >
                    {actions.map((action) => (
                        <button
                            key={action.next}
                            type="button"
                            disabled={isUpdating}
                            onClick={() => handleAction(action)}
                            style={{
                              background:
                                  action.next === 'CANCELLED'
                                      ? '#fff0ed'
                                      : '#153d30',
                              color:
                                  action.next === 'CANCELLED'
                                      ? '#c0392b'
                                      : '#ffffff',
                              border: 'none',
                              borderRadius: 9,
                              padding: '10px 16px',
                              cursor: isUpdating ? 'wait' : 'pointer',
                              fontWeight: 600
                            }}
                        >
                          {isUpdating ? 'Saving...' : action.label}
                        </button>
                    ))}
                  </div>
              )}

              {actions.length === 0 && (
                  <p style={{ marginTop: 12, opacity: 0.7 }}>
                    No merchant actions available for this status.
                  </p>
              )}
            </div>
          </td>
        </tr>
    )}
  </>
}