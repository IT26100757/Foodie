// Optional host integration: assign a provider to window.FOODIE_TRACKING_PROVIDER
// with subscribeToOrder(orderId, onUpdate), updateOrder(order), and
// confirmDelivery(order). The customer portal stays honest when no provider exists.
function getProvider() {
  return typeof window !== 'undefined' ? window.FOODIE_TRACKING_PROVIDER : null
}

export function subscribeToOrder(orderId, onUpdate, onUnavailable) {
  const provider = getProvider()
  if (!provider?.subscribeToOrder) return () => {}
  try {
    return provider.subscribeToOrder(orderId, onUpdate) || (() => {})
  } catch (error) {
    onUnavailable?.(error)
    return () => {}
  }
}

export function mountTrackingMap(element, order, onLocation, onUnavailable) {
  const provider = getProvider()
  if (!provider?.mountMap) return null
  try {
    return provider.mountMap(element, { order, onLocation }) || null
  } catch (error) {
    onUnavailable?.(error)
    return null
  }
}

export async function saveOrderUpdate(order) {
  const provider = getProvider()
  if (!provider?.updateOrder) return { connected: false }
  await provider.updateOrder(order)
  return { connected: true }
}

export async function confirmDelivery(order) {
  const provider = getProvider()
  if (!provider?.confirmDelivery) return { connected: false }
  const result = await provider.confirmDelivery(order)
  return { connected: true, result }
}
