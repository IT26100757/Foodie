import { useEffect, useMemo, useRef, useState } from 'react'
import './WalletPage.css'
import WalletReports from './WalletReports.jsx'

const sriLankanBanks = ['Bank of Ceylon', 'Commercial Bank', 'Hatton National Bank', "People's Bank", 'Nations Trust Bank', 'Sampath Bank', 'Seylan Bank', 'Amana Bank']
const bankLogoCrops = {
  'bank of ceylon': { x: 168, y: 140, width: 210, height: 210 },
  'commercial bank': { x: 742, y: 153, width: 154, height: 118 },
  'hatton national bank': { x: 26, y: 515, width: 133, height: 137 },
  hnb: { x: 26, y: 515, width: 133, height: 137 },
  "people's bank": { x: 607, y: 488, width: 139, height: 135 },
  'peoples bank': { x: 607, y: 488, width: 139, height: 135 },
  'nations trust bank': { x: 7, y: 784, width: 141, height: 137 },
  'sampath bank': { x: 590, y: 805, width: 158, height: 155 },
  'seylan bank': { x: 39, y: 1038, width: 136, height: 214 },
  'amana bank': { x: 980, y: 1047, width: 97, height: 104 },
}
const money = (value) => `Rs. ${Math.abs(value).toLocaleString('en-LK')}`
const moneyWithCents = (value) => `Rs. ${Math.abs(Number(value)).toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`

function WalletPage({ orders = [] }) {
  const [accounts, setAccounts] = useState([])
  const [transferTransactions, setTransferTransactions] = useState([])
  const [earningTransactions, setEarningTransactions] = useState([])
  const [recordedEarnings, setRecordedEarnings] = useState(null)
  const [earningsError, setEarningsError] = useState('')
  const transactions = useMemo(() =>
          [...transferTransactions, ...earningTransactions]
              .sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0)),
      [transferTransactions, earningTransactions]
  )
  const [dialog, setDialog] = useState('')
  const [reportsOpen, setReportsOpen] = useState(false)
  const [notice, setNotice] = useState('')
  const [accountForm, setAccountForm] = useState({ bank: '', name: '', number: '', branch: '' })
  const [transferAmount, setTransferAmount] = useState('')
  const [transferError, setTransferError] = useState('')
  const [accountError, setAccountError] = useState('')
  const [transferBusy, setTransferBusy] = useState(false)
  const [activeTransaction, setActiveTransaction] = useState(null)
  const [selectedAccount, setSelectedAccount] = useState('')
  const [metric, setMetric] = useState('All')
  const [summaryPeriod, setSummaryPeriod] = useState('This month')
  const [chartPeriod, setChartPeriod] = useState('Last 6 months')
  const walletSummary = useMemo(() => {
    const now = new Date()

    const start = new Date(now.getFullYear(), now.getMonth(), 1)
    const end = new Date(now.getFullYear(), now.getMonth() + 1, 1)

    if (summaryPeriod === 'Last month') {
      start.setMonth(start.getMonth() - 1)
      end.setMonth(end.getMonth() - 1)
    } else if (summaryPeriod === 'Last 3 months') {
      start.setMonth(start.getMonth() - 2)
    } else if (summaryPeriod === 'Last 6 months') {
      start.setMonth(start.getMonth() - 5)
    }

    const deliveredOrders = orders.filter((order) => {
      const status = String(order.status || '').toUpperCase()
      const date = new Date(order.createdAt || order.date)

      return (
          status === 'DELIVERED' &&
          !Number.isNaN(date.getTime()) &&
          date >= start &&
          date < end
      )
    })

    const sales = deliveredOrders.reduce((sum, order) => {
      const amount = Number(order.totalAmount ?? order.total ?? 0)
      return sum + (Number.isFinite(amount) ? amount : 0)
    }, 0)

    const serviceCharge = Math.round(sales * 0.10 * 100) / 100

    return {
      sales,
      serviceCharge,
      netEarnings: sales - serviceCharge,
      orderCount: deliveredOrders.length
    }
  }, [orders, summaryPeriod])


  const chartData = useMemo(() => {
    const now = new Date()
    const monthCount = chartPeriod === 'Last 3 months' ? 3 : 6

    const months = chartPeriod === 'This year'
        ? Array.from({ length: 12 }, (_, i) =>
            new Date(now.getFullYear(), i, 1)
        )
        : Array.from({ length: monthCount }, (_, i) =>
            new Date(now.getFullYear(), now.getMonth() - monthCount + i + 1, 1)
        )

    return months.map((month) => {
      const sales = orders.reduce((sum, order) => {
        if (String(order.status).toUpperCase() !== 'DELIVERED') return sum

        const date = new Date(order.createdAt || order.date)
        if (Number.isNaN(date.getTime())) return sum

        if (
            date.getFullYear() !== month.getFullYear() ||
            date.getMonth() !== month.getMonth()
        ) return sum

        const amount = Number(order.totalAmount ?? order.total ?? 0)
        return sum + (Number.isFinite(amount) ? amount : 0)
      }, 0)

      const charge = Math.round(sales * 0.1 * 100) / 100

      return {
        label: month.toLocaleString('en-US', { month: 'short' }),
        fullLabel: month.toLocaleString('en-US', {
          month: 'long',
          year: 'numeric'
        }),
        sales,
        charge,
        net: sales - charge
      }
    })
  }, [orders, chartPeriod])

  const chartMax = Math.max(1, ...chartData.map((item) => item.sales))
  const hasChartData = chartData.some((item) => item.sales > 0)



  useEffect(() => {
    let cancelled = false

    async function loadBankAccounts() {
      try {
        const response = await fetch('/api/wallet/bank-accounts', {
          credentials: 'include',
        })

        if (!response.ok) {
          throw new Error('Unable to load bank accounts.')
        }

        const data = await response.json()

        if (cancelled) return

        if (!Array.isArray(data)) {
          throw new Error('Invalid bank account response.')
        }

        setAccounts(data)

        setSelectedAccount((current) => {
          if (data.some((account) => String(account.id) === current)) {
            return current
          }

          return data.length ? String(data[0].id) : ''
        })
      } catch (error) {
        if (!cancelled) {
          console.error('Bank accounts:', error)
          setAccountError(error.message)
        }
      }
    }

    loadBankAccounts()

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false

    async function loadTransactions() {
      try {
        const response = await fetch('/api/wallet/transfers', {
          credentials: 'include',
        })

        if (!response.ok) {
          throw new Error('Unable to load wallet transactions')
        }

        const items = await response.json()

        if (cancelled || !Array.isArray(items)) return

        setTransferTransactions(
            items.map((item) => ({
              id: item.transactionId,
              timestamp: Date.parse(item.updatedAt) || 0,
              title: `Transfer to ${item.bankName}`,
              date: new Intl.DateTimeFormat('en-LK', {
                dateStyle: 'medium',
              }).format(new Date(item.updatedAt)),
              kind: 'Transfer',
              amount: -Number(item.amount),
              status:
                  item.status === 'COMPLETED'
                      ? 'Completed'
                      : item.status === 'FAILED'
                          ? 'Failed'
                          : item.status === 'PENDING'
                              ? 'Pending'
                              : 'Processing',
              currency: item.currency,
            }))
        )
      } catch (error) {
        console.error('Wallet transactions:', error)
      }
    }

    loadTransactions()

    return () => {
      cancelled = true
    }
  }, [])
  useEffect(() => {
    let cancelled = false

    async function loadRecordedEarnings() {
      try {
        const response = await fetch('/api/wallet/earnings', {
          credentials: 'include',
        })
        if (!response.ok) throw new Error('Unable to load recorded earnings.')
        const data = await response.json()
        if (!Array.isArray(data.transactions)) {
          throw new Error('Invalid earnings response.')
        }
        if (cancelled) return

        setRecordedEarnings(Number(data.recordedEarnings) || 0)
        setEarningsError('')
        setEarningTransactions(data.transactions.map((entry) => ({
          id: `earning-${entry.id}`,
          timestamp: Date.parse(entry.createdAt) || 0,
          title: `Recorded earning · Order #${entry.orderId}`,
          date: entry.createdAt
              ? new Intl.DateTimeFormat('en-LK', { dateStyle: 'medium' }).format(new Date(entry.createdAt))
              : '—',
          kind: 'Earning',
          amount: Number(entry.amount) || 0,
          status: 'Recorded',
          currency: entry.currency || data.currency || 'LKR',
        })))
      } catch (error) {
        if (!cancelled) {
          console.error('Recorded earnings:', error)
          setEarningsError('Recorded earnings could not be loaded.')
        }
      }
    }

    loadRecordedEarnings()
    return () => { cancelled = true }
  }, [])

  const showNotice = (message) => { setNotice(message); window.setTimeout(() => setNotice(''), 4200) }
  function openAccountDialog() {
    setAccountForm({
      bank: '',
      name: '',
      number: '',
      branch: ''
    })
    setAccountError('')
    setDialog('account')
  }

  async function removeAccount(accountId) {
    const confirmed = window.confirm(
        'Are you sure you want to remove this bank account?'
    )

    if (!confirmed) return

    try {
      // Get CSRF token from Spring Security.
      const csrfResponse = await fetch('/api/csrf', {
        credentials: 'include',
      })

      if (!csrfResponse.ok) {
        throw new Error('Unable to verify your session.')
      }

      const csrf = await csrfResponse.json()

      if (!csrf.token) {
        throw new Error('CSRF token is missing.')
      }

      // Delete the bank account from the backend.
      const response = await fetch(
          `/api/wallet/bank-accounts/${encodeURIComponent(accountId)}`,
          {
            method: 'DELETE',
            credentials: 'include',
            headers: {
              [csrf.headerName || 'X-CSRF-TOKEN']: csrf.token,
            },
          }
      )

      if (!response.ok) {
        const result = await response.json().catch(() => null)

        throw new Error(
            result?.detail ||
            result?.message ||
            'Unable to remove bank account.'
        )
      }

      // Update the existing UI after successful deletion.
      const updated = accounts.filter(
          (account) => String(account.id) !== String(accountId)
      )

      setAccounts(updated)

      if (String(accountId) === selectedAccount) {
        setSelectedAccount(
            updated.length ? String(updated[0].id) : ''
        )
      }

      showNotice('Bank account removed successfully.')
    } catch (error) {
      console.error('Remove bank account:', error)

      showNotice(
          error.message || 'Unable to remove bank account.'
      )
    }
  }


  async function addAccount(event) {
    event.preventDefault()

    const { bank, name, number, branch } = accountForm
    const cleanNumber = number.replace(/\s/g, '')

    if (!bank) {
      setAccountError('Please select a bank.')
      return
    }

    if (!name.trim()) {
      setAccountError('Enter the account holder name.')
      return
    }

    if (!/^\d{4,20}$/.test(cleanNumber)) {
      setAccountError('Enter a valid numeric account number.')
      return
    }

    if (!branch.trim()) {
      setAccountError('Enter your bank branch.')
      return
    }

    const possibleDuplicate = accounts.some((account) =>
        account.bank === bank &&
        account.name.trim().toLowerCase() === name.trim().toLowerCase() &&
        account.number.replace(/\D/g, '').slice(-4) === cleanNumber.slice(-4)
    )

    if (possibleDuplicate) {
      setAccountError('This bank account may already be linked.')
      return
    }

    setAccountError('')

    try {
      // Get CSRF token from Spring Security.
      const csrfResponse = await fetch('/api/csrf', {
        credentials: 'include',
      })

      if (!csrfResponse.ok) {
        throw new Error('Unable to verify your session.')
      }

      const csrf = await csrfResponse.json()

      if (!csrf.token) {
        throw new Error('CSRF token is missing.')
      }

      // Save the account securely through Spring Boot.
      const response = await fetch('/api/wallet/bank-accounts', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          [csrf.headerName || 'X-CSRF-TOKEN']: csrf.token,
        },
        body: JSON.stringify({
          bank,
          name: name.trim(),
          number: cleanNumber,
          branch: branch.trim(),
        }),
      })

      const result = await response.json().catch(() => null)

      if (!response.ok) {
        throw new Error(
            result?.detail ||
            result?.message ||
            'Unable to save bank account.'
        )
      }

      // The backend returns the masked account number.
      setAccounts((current) => [result, ...current])
      setSelectedAccount(String(result.id))

      // Clear the form, including the full account number.
      setAccountForm({
        bank: '',
        name: '',
        number: '',
        branch: '',
      })

      setAccountError('')
      setDialog('')

      showNotice('Bank account added successfully.')
    } catch (error) {
      console.error('Add bank account:', error)

      setAccountError(
          error.message || 'Unable to save bank account.'
      )
    }
  }

  async function requestTransfer(event) {
    event.preventDefault()

    if (transferBusy) return

    const account =
        accounts.find(
            (item) => String(item.id) === selectedAccount
        ) || accounts[0]

    if (!account) {
      setTransferError(
          'Add a bank account before requesting a transfer.'
      )
      return
    }

    const amount = Number(transferAmount)

    if (!Number.isFinite(amount) || amount <= 0) {
      setTransferError('Enter a valid transfer amount.')
      return
    }

    setTransferBusy(true)
    setTransferError('')

    try {
      // Retrieve CSRF token from Spring Security.
      const csrfResponse = await fetch('/api/csrf', {
        credentials: 'include',
      })

      if (!csrfResponse.ok) {
        throw new Error('Unable to verify your session.')
      }

      const csrf = await csrfResponse.json()

      if (!csrf.token) {
        throw new Error('CSRF token is missing.')
      }

      const response = await fetch('/api/wallet/transfers', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          [csrf.headerName || 'X-CSRF-TOKEN']: csrf.token,
        },
        body: JSON.stringify({
          bankName: account.bank,
          accountHolder: account.name,
          accountLastFour: account.number
              .replace(/\D/g, '')
              .slice(-4),
          amount,
        }),
      })

      const result = await response.json().catch(() => null)

      if (!response.ok) {
        throw new Error(
            result?.detail ||
            result?.message ||
            'Unable to submit transfer request.'
        )
      }

      const record = {
        id: result.transactionId,
        title: `Transfer to ${result.bankName}`,
        date: new Intl.DateTimeFormat('en-LK', {
          dateStyle: 'medium',
        }).format(new Date(result.updatedAt)),
        kind: 'Transfer',
        amount: -Number(result.amount),
        status:
            result.status === 'COMPLETED'
                ? 'Completed'
                : result.status === 'FAILED'
                    ? 'Failed'
                    : result.status === 'PENDING'
                        ? 'Pending'
                        : 'Processing',
        currency: result.currency,
      }

      setTransferTransactions((current) => [
        { ...record, timestamp: Date.parse(result.updatedAt) || Date.now() },
        ...current,
      ])

      setTransferAmount('')

// Store the complete backend response for the existing receipt popup.
      setActiveTransaction(result)

// Open the existing receipt dialog.
      if (result.status === 'COMPLETED' || result.status === 'FAILED') {
        setDialog('receipt')
      } else {
        setDialog('')
        showNotice('Transfer request recorded. No bank payment has been confirmed.')
      }

    } catch (error) {
      console.error('Transfer request:', error)

      setTransferError(
          error.message || 'Unable to submit transfer request.'
      )

      setDialog('transfer')
    } finally {
      setTransferBusy(false)
    }
  }

  function printReceipt() {
    document.body.classList.add('wallet-print-receipt')
    window.print()
    window.setTimeout(() => document.body.classList.remove('wallet-print-receipt'), 800)
  }

  return <div className="dashboard-content wallet-content">
    <div className="dashboard-heading-row wallet-heading"><div><span className="dashboard-kicker">YOUR MONEY, MADE CLEAR</span><h1>Merchant Wallet<span>.</span></h1><p>See your earnings, service charge, and settlement activity in one place.</p></div><button type="button" className="wallet-report-open" onClick={() => setReportsOpen(true)}><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 20V10h4v10M10 20V4h4v16M16 20v-8h4v8M3 20h18" /></svg><span>Generate report</span></button></div>
    <section className="wallet-balance-card">
      <div className="wallet-balance-copy"><span className="wallet-overline">FOODIE BANK BALANCE <i>•</i> AVAILABLE</span><strong>—</strong><p>Balance information is not available.</p></div>
      <div className="wallet-balance-actions"><button type="button" className="wallet-transfer-button" onClick={() => setDialog('transfer')}><span>↗</span> Transfer to bank</button><button type="button" className="wallet-transactions-link" onClick={() => document.getElementById('wallet-transactions')?.scrollIntoView({ behavior: 'smooth' })}>View transactions <span>↓</span></button></div>
      <div className="wallet-card-decoration" aria-hidden="true"><i /><i /><i /></div>
    </section>
    <div className="wallet-section-label"><div><span className="wallet-section-kicker">MONTHLY SUMMARY</span><h2>Monthly summary</h2></div><PeriodPicker label="SUMMARY PERIOD" value={summaryPeriod} options={['This month', 'Last month', 'Last 3 months', 'Last 6 months']} onChange={setSummaryPeriod} /></div>
    <div className="wallet-summary-grid">

      <article className="wallet-summary-card">
        <div className="wallet-summary-top">
          <span>Total Sales</span>
          <i className="wallet-summary-icon sales">↗</i>
        </div>
        <strong>{moneyWithCents(walletSummary.sales)}</strong>
        <small>{walletSummary.orderCount} delivered orders</small>
      </article>

      <article className="wallet-summary-card">
        <div className="wallet-summary-top">
          <span>Our Service Charge <em>10%</em></span>
          <i className="wallet-summary-icon fee">%</i>
        </div>
        <strong>{moneyWithCents(walletSummary.serviceCharge)}</strong>
        <small>Estimated platform commission</small>
      </article>

      <article className="wallet-summary-card">
        <div className="wallet-summary-top">
          <span>Net Earnings</span>
          <i className="wallet-summary-icon net">✓</i>
        </div>
        <strong>{moneyWithCents(walletSummary.netEarnings)}</strong>
        <small>Estimated earnings before settlement</small>
      </article>

    </div>
    <div className="wallet-main-grid">
      <div className="wallet-left-column">
        <section className="dashboard-card wallet-analysis-card"><div className="wallet-card-heading"><div><span className="wallet-section-kicker">SALES & SETTLEMENTS</span><h2>Business Analysis</h2><p>Sales and settlement data will appear here.</p></div><PeriodPicker label="ANALYSIS PERIOD" value={chartPeriod} options={['Last 3 months', 'Last 6 months', 'This year']} onChange={setChartPeriod} /></div>
          <div className="wallet-chart-legend"><button type="button" className={metric === 'All' || metric === 'Sales' ? 'chosen' : ''} onClick={() => setMetric(metric === 'Sales' ? 'All' : 'Sales')}><i className="legend-sales" />Sales</button><button type="button" className={metric === 'All' || metric === 'Charge' ? 'chosen' : ''} onClick={() => setMetric(metric === 'Charge' ? 'All' : 'Charge')}><i className="legend-charge" />Service charge</button><button type="button" className={metric === 'All' || metric === 'Net' ? 'chosen' : ''} onClick={() => setMetric(metric === 'Net' ? 'All' : 'Net')}><i className="legend-net" />Net earnings</button></div>

          {hasChartData ? (
              <div style={{
                display: 'flex',
                alignItems: 'flex-end',
                justifyContent: 'space-around',
                gap: 12,
                height: 260,
                padding: '20px 8px 8px'
              }}>
                {chartData.map((item, index) => (
                    <div
                        key={index}
                        className="wallet-chart-month"
                        tabIndex={0}
                        aria-label={`${item.fullLabel}: Sales ${moneyWithCents(item.sales)}, Service charge ${moneyWithCents(item.charge)}, Net earnings ${moneyWithCents(item.net)}`}
                        style={{
                          flex: 1,
                          height: '100%',
                          display: 'flex',
                          flexDirection: 'column',
                          alignItems: 'center',
                          justifyContent: 'flex-end',
                          gap: 8
                        }}>
                      <div style={{
                        display: 'flex',
                        alignItems: 'flex-end',
                        justifyContent: 'center',
                        gap: 4,
                        width: '100%',
                        height: 210
                      }}>
                        {[
                          { key: 'sales', name: 'Sales', color: '#ff6934' },
                          { key: 'charge', name: 'Charge', color: '#f5ba57' },
                          { key: 'net', name: 'Net', color: '#153d30' }
                        ]
                            .filter((bar) => metric === 'All' || metric === bar.name)
                            .map((bar) => (
                                <div
                                    key={bar.key}
                                    title={`${bar.name}: ${moneyWithCents(item[bar.key])}`}
                                    style={{
                                      width: metric === 'All' ? '25%' : '55%',
                                      maxWidth: 32,
                                      height: `${item[bar.key] > 0
                                          ? Math.max(3, item[bar.key] / chartMax * 100)
                                          : 0}%`,
                                      background: bar.color,
                                      borderRadius: '6px 6px 0 0',
                                      transition: 'height .25s ease'
                                    }}
                                />
                            ))}
                      </div>


                      <div
                          className={`wallet-chart-tooltip ${
                              index === 0 ? 'tooltip-first' :
                                  index === chartData.length - 1 ? 'tooltip-last' : ''
                          }`}
                      >
                        <strong>{item.fullLabel}</strong>

                        <div className="wallet-tooltip-row">
                          <span><i className="tooltip-sales" /> Sales</span>
                          <b>{moneyWithCents(item.sales)}</b>
                        </div>

                        <div className="wallet-tooltip-row">
                          <span><i className="tooltip-charge" /> Service charge</span>
                          <b>{moneyWithCents(item.charge)}</b>
                        </div>

                        <div className="wallet-tooltip-row">
                          <span><i className="tooltip-net" /> Net earnings</span>
                          <b>{moneyWithCents(item.net)}</b>
                        </div>
                      </div>


                      <span style={{
                        fontSize: 11,
                        color: '#877f75',
                        fontWeight: 600
                      }}>
          {item.label}
        </span>
                    </div>
                ))}
              </div>
          ) : (
              <div className="wallet-chart-empty">
                No delivered orders found for this period.
              </div>
          )}

        </section>
        <section className="dashboard-card wallet-transactions-card" id="wallet-transactions"><div className="wallet-card-heading"><div><span className="wallet-section-kicker">YOUR WALLET ACTIVITY</span><h2>Recent Transactions</h2>{recordedEarnings !== null && <small>Recorded payment earnings: {moneyWithCents(recordedEarnings)} · Not available for transfer</small>}</div></div>
          {earningsError && <small role="alert" className="wallet-field-error">{earningsError}</small>}
          {transactions.length ? <div className="wallet-table-wrap"><table className="wallet-table"><thead><tr><th>TRANSACTION</th><th>DATE</th><th>TYPE</th><th>AMOUNT</th><th>STATUS</th></tr></thead><tbody>{transactions.map((item) => <tr key={item.id}><td><span className={`wallet-transaction-mark ${item.amount < 0 ? 'charge-mark' : ''}`}>{item.amount < 0 ? '−' : '+'}</span><span className="wallet-transaction-name"><b>{item.title}</b><small>{item.id}</small></span></td><td>{item.date}</td><td>{item.kind}</td><td className={item.amount < 0 ? 'negative-amount' : 'positive-amount'}>{item.amount < 0 ? '−' : '+'} {money(item.amount)}</td><td><span className={`wallet-status ${item.status === 'Completed' ? 'complete' : 'deducted'}`}><i />{item.status}</span></td></tr>)}</tbody></table></div> : <div className="wallet-chart-empty">No transactions found.</div>}
        </section>
      </div>
      <aside className="wallet-right-column">
        <section className="dashboard-card wallet-add-bank"><div className="wallet-side-title"><span className="wallet-side-icon">＋</span><div><h2>Add Bank Account</h2><p>Link an account for settlements</p></div></div><button type="button" className="wallet-add-button" onClick={openAccountDialog}>＋ Add a bank account</button><small>Securely manage where your funds go.</small></section>
        <section className="dashboard-card wallet-accounts"><div className="wallet-card-heading"><div><span className="wallet-section-kicker">SETTLEMENT DETAILS</span><h2>Your Bank Accounts</h2></div></div><div className="wallet-account-list">{accounts.length ? accounts.map((account) => <article className="wallet-account" key={account.id}><BankLogo bank={account.bank} /><span className="wallet-account-copy"><b>{account.bank}</b><small>{account.name} · {account.number}</small></span><button type="button" aria-label={`Transfer to ${account.bank}`} onClick={() => { setSelectedAccount(String(account.id)); setDialog('transfer') }}>↗</button><button
            type="button"
            className="wallet-remove-account"
            title="Remove bank account"
            aria-label={`Remove ${account.bank} account`}
            onClick={() => removeAccount(account.id)}
        >
          ×
        </button></article>) : <div className="wallet-empty-accounts">No bank accounts added.</div>}</div><button type="button" className="wallet-manage-accounts" onClick={openAccountDialog}>Manage accounts <span>→</span></button></section>
        <section className="wallet-transfer-promo"><div className="wallet-promo-icon">↗</div><span className="wallet-section-kicker">QUICK SETTLEMENT</span><h2>Move your earnings</h2><p>Transfer your available balance to any linked bank account.</p><button type="button" onClick={() => setDialog('transfer')}>Transfer to bank <span>→</span></button><i className="wallet-promo-orb" /></section>
      </aside>
    </div>
    {reportsOpen && <WalletReports orders={orders} onClose={() => setReportsOpen(false)} />}
    {notice && <div className="wallet-toast" role="status">✓ <span>{notice}</span><button type="button" onClick={() => setNotice('')} aria-label="Dismiss">×</button></div>}
    {dialog && <div className="wallet-dialog-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget && !transferBusy) setDialog('') }}>
      {dialog === 'processing' ? <section className="wallet-dialog wallet-processing-dialog" role="status" aria-live="polite" aria-busy="true"><div className="wallet-processing-spinner" /><span className="wallet-section-kicker">FOODIE MERCHANT WALLET</span><h2>Transfer processing</h2><p>Your transaction is being recorded and checked. Keep this window open for the result.</p><div className="wallet-processing-steps"><span className="done">✓ Request received</span><span className="active"><i /> Checking destination bank</span><span>Confirmation pending</span></div></section>
          : dialog === 'receipt' && activeTransaction ? <section className={`wallet-dialog wallet-receipt ${activeTransaction.status === 'COMPLETED' ? 'receipt-success' : 'receipt-failed'}`} role="dialog" aria-modal="true" aria-labelledby="wallet-receipt-title"><button type="button" className="wallet-dialog-close" onClick={() => setDialog('')} aria-label="Close receipt">×</button><div className="wallet-receipt-brand"><span>foodie<span>.</span></span><small>MERCHANT WALLET</small></div><h2 id="wallet-receipt-title">Transfer receipt</h2><div className="wallet-receipt-state"><span>{activeTransaction.status === 'COMPLETED' ? '✓' : '!'}</span><b>{activeTransaction.status === 'COMPLETED' ? 'COMPLETED' : 'FAILED'}</b><strong>{moneyWithCents(activeTransaction.amount)}</strong></div><div className="wallet-receipt-section"><span>TO</span><b>{activeTransaction.bankName}</b><small>{activeTransaction.accountHolder}<br />Account ending in {activeTransaction.accountLastFour}</small></div><div className="wallet-receipt-section"><span>TRANSACTION DETAILS</span><dl><div><dt>Date</dt><dd>{new Intl.DateTimeFormat('en-LK', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(activeTransaction.updatedAt))}</dd></div><div><dt>Transaction ID</dt><dd className="wallet-receipt-id">{activeTransaction.transactionId}</dd></div><div><dt>Status</dt><dd>{activeTransaction.status === 'COMPLETED' ? 'Completed' : 'Failed'}</dd></div>{activeTransaction.failureReason && <div><dt>Reason</dt><dd>{activeTransaction.failureReason}</dd></div>}</dl></div><div className="wallet-receipt-section"><span>FUNDING SOURCE</span><b>Foodie Bank Balance</b></div><div className="wallet-receipt-actions"><button type="button" className="wallet-receipt-secondary" onClick={printReceipt}>Print receipt</button><button type="button" className="wallet-dialog-submit" onClick={() => setDialog('')}>Done</button></div></section>
              : <section className="wallet-dialog" role="dialog" aria-modal="true" aria-labelledby="wallet-dialog-title"><button type="button" className="wallet-dialog-close" onClick={() => setDialog('')} aria-label="Close">×</button><span className="wallet-section-kicker">FOODIE MERCHANT WALLET</span><h2 id="wallet-dialog-title">{dialog === 'account' ? 'Add a bank account' : 'Transfer to bank'}</h2><p>{dialog === 'account' ? 'Securely link a bank account for future settlements.' : 'Transfer funds to a linked bank account.'}</p>{dialog === 'account' ? <form onSubmit={addAccount} className="wallet-dialog-form">
                <label>
                  Bank name
                  <BankNamePicker
                      value={accountForm.bank}
                      onSelect={(bank) => {
                        setAccountForm((current) => ({
                          ...current,
                          bank,
                          branch: ''
                        }))
                        setAccountError('')
                      }}
                  />
                </label>
                <label>Account holder<input required value={accountForm.name} onChange={(event) => setAccountForm({ ...accountForm, name: event.target.value })} placeholder="Name on account" /></label><label>Account number<input required inputMode="numeric" pattern="[0-9]{4,20}" minLength="4" maxLength="20" title="Enter a 4 to 20 digit account number" value={accountForm.number} onChange={(event) => setAccountForm({ ...accountForm, number: event.target.value })} placeholder="Enter account number" /></label><label>
                Bank branch
                <input
                    required
                    disabled={!accountForm.bank}
                    value={accountForm.branch}
                    onChange={(event) =>
                        setAccountForm({
                          ...accountForm,
                          branch: event.target.value
                        })
                    }
                    placeholder="Enter your branch name"
                />
              </label>
                {accountError && (
                    <small className="wallet-field-error" role="alert">
                      {accountError}
                    </small>
                )}
                <button className="wallet-dialog-submit" type="submit">Save account</button></form> : <form onSubmit={requestTransfer} className="wallet-dialog-form">
                <label>
                  To bank account
                  <BankAccountPicker
                      accounts={accounts}
                      selectedAccount={selectedAccount}
                      onSelect={setSelectedAccount}
                  />
                </label><label>Amount<input required type="number" min="1" step="0.01" value={transferAmount} onChange={(event) => setTransferAmount(event.target.value)} placeholder="Enter amount" /></label>{transferError && <div className="wallet-transfer-error" role="alert">{transferError}</div>}<button className="wallet-dialog-submit" type="submit" disabled={transferBusy}>Review transfer</button></form>}</section>}
    </div>}
  </div>
}


function PeriodPicker({ label, value, options, onChange }) {
  const [open, setOpen] = useState(false)
  const pickerRef = useRef(null)

  useEffect(() => {
    if (!open) return undefined
    function closeOutside(event) {
      if (!pickerRef.current?.contains(event.target)) setOpen(false)
    }
    function closeOnEscape(event) {
      if (event.key === 'Escape') setOpen(false)
    }
    document.addEventListener('pointerdown', closeOutside)
    document.addEventListener('keydown', closeOnEscape)
    return () => {
      document.removeEventListener('pointerdown', closeOutside)
      document.removeEventListener('keydown', closeOnEscape)
    }
  }, [open])

  return <div className="wallet-period-picker" ref={pickerRef}>
    <button type="button" className="wallet-period" aria-haspopup="listbox" aria-expanded={open} onClick={() => setOpen((current) => !current)}>{value}<span className="wallet-period-chevron">⌄</span></button>
    {open && <div className="wallet-period-menu" role="listbox" aria-label={label}><span className="wallet-period-menu-label">{label}</span>{options.map((option) => <button type="button" role="option" aria-selected={value === option} className={value === option ? 'selected' : ''} key={option} onClick={() => { onChange(option); setOpen(false) }}><span>{option}</span><b>{value === option ? '✓' : ''}</b></button>)}</div>}
  </div>
}

function BankLogo({ bank }) {
  const normalizedBank = bank.trim().toLowerCase().replace(/[’‘]/g, "'")
  const crop = Object.entries(bankLogoCrops).find(([name]) => normalizedBank.includes(name))?.[1]
  const scale = crop ? 26 / Math.max(crop.width, crop.height) : 0
  const logoStyle = crop ? {
    '--bank-sheet-size': `${1080 * scale}px ${1349 * scale}px`,
    '--bank-logo-position': `-${crop.x * scale}px -${crop.y * scale}px`,
  } : undefined
  return <span className={`wallet-bank-mark ${crop ? 'has-sprite-logo' : ''}`} style={logoStyle} role="img" aria-label={`${bank} logo`}>{!crop && <span>{bank.slice(0, 1).toUpperCase()}</span>}</span>
}

function BankNamePicker({ value, onSelect }) {
  const [open, setOpen] = useState(false)
  const pickerRef = useRef(null)
  const selected = sriLankanBanks.find((bank) => bank === value)

  useEffect(() => {
    if (!open) return undefined
    function closeOutside(event) {
      if (!pickerRef.current?.contains(event.target)) setOpen(false)
    }
    function closeOnEscape(event) {
      if (event.key === 'Escape') setOpen(false)
    }
    document.addEventListener('pointerdown', closeOutside)
    document.addEventListener('keydown', closeOnEscape)
    return () => {
      document.removeEventListener('pointerdown', closeOutside)
      document.removeEventListener('keydown', closeOnEscape)
    }
  }, [open])

  return <div className="wallet-bank-name-picker" ref={pickerRef}>
    <button type="button" className="wallet-bank-picker-trigger" aria-haspopup="listbox" aria-expanded={open} onClick={() => setOpen((current) => !current)}>{selected ? <BankLogo bank={selected} /> : <span className="wallet-bank-name-placeholder">⌕</span>}<span className="wallet-bank-picker-copy"><b>{selected || 'Choose a bank'}</b><small>{selected ? 'Sri Lankan bank' : 'Select from available banks'}</small></span><span className="wallet-period-chevron">⌄</span></button>
    {open && <div className="wallet-bank-picker-menu wallet-bank-name-menu" role="listbox" aria-label="Select a Sri Lankan bank">{sriLankanBanks.map((bank) => <button type="button" role="option" aria-selected={selected === bank} className={selected === bank ? 'selected' : ''} key={bank} onClick={() => { onSelect(bank); setOpen(false) }}><BankLogo bank={bank} /><span className="wallet-bank-picker-copy"><b>{bank}</b><small>Bank account in Sri Lanka</small></span><span className="wallet-bank-option-check">{selected === bank ? '✓' : ''}</span></button>)}</div>}
  </div>
}

function BankAccountPicker({ accounts, selectedAccount, onSelect }) {
  const [open, setOpen] = useState(false)
  const pickerRef = useRef(null)
  const selected = accounts.find((account) => String(account.id) === selectedAccount) || accounts[0]

  useEffect(() => {
    if (!open) return undefined
    function closeOutside(event) {
      if (!pickerRef.current?.contains(event.target)) setOpen(false)
    }
    function closeOnEscape(event) {
      if (event.key === 'Escape') setOpen(false)
    }
    document.addEventListener('pointerdown', closeOutside)
    document.addEventListener('keydown', closeOnEscape)
    return () => {
      document.removeEventListener('pointerdown', closeOutside)
      document.removeEventListener('keydown', closeOnEscape)
    }
  }, [open])

  if (!selected) return <div className="wallet-dialog-note">Add a bank account before transferring.</div>
  return <div className="wallet-bank-picker" ref={pickerRef}>
    <button type="button" className="wallet-bank-picker-trigger" aria-haspopup="listbox" aria-expanded={open} onClick={() => setOpen((current) => !current)}><BankLogo bank={selected.bank} /><span className="wallet-bank-picker-copy"><b>{selected.bank} · {selected.number}</b><small>{selected.name}</small></span><span className="wallet-period-chevron">⌄</span></button>
    {open && <div className="wallet-bank-picker-menu" role="listbox" aria-label="Choose a bank account">{accounts.map((account) => { const selectedOption = String(account.id) === selectedAccount; return <button type="button" role="option" aria-selected={selectedOption} className={selectedOption ? 'selected' : ''} key={account.id} onClick={() => { onSelect(String(account.id)); setOpen(false) }}><BankLogo bank={account.bank} /><span className="wallet-bank-picker-copy"><b>{account.bank}</b><small>{account.name} · {account.number}</small></span><span className="wallet-bank-option-check">{selectedOption ? '✓' : ''}</span></button> })}</div>}
  </div>
}

export default WalletPage
