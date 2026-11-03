# Wallet API

The wallet API persists transfer attempts in the configured database and exposes them through `GET /api/wallet/transfers`. Each attempt receives a unique transaction ID.

No bank transfer provider is configured. Transfer requests are recorded and marked failed with a provider-unavailable reason; the API does not move funds or report a simulated success. Configure a real provider before enabling settlements.
