
import { useEffect, useState } from 'react'
import './App.css'

const foodieLogo = 'https://res.cloudinary.com/g7u1tdxx/image/upload/v1792826258/Gemini_Generated_Image_v0q9c4v0q9c4v0q9.jpg'

function App() {
  const [view, setView] = useState('splash')
  const [merchantName, setMerchantName] = useState('')

  useEffect(() => {
    const timer = window.setTimeout(() => setView('login'), 1800)
    return () => window.clearTimeout(timer)
  }, [])

  if (view === 'splash') return <SplashScreen />
  if (view === 'registration') return <RegistrationPortal onLogin={() => setView('login')} />
  if (view === 'dashboard') return <MerchantDashboard merchantName={merchantName} onSignOut={() => setView('login')} />
  if (view === 'welcome') return <WelcomeTransition merchantName={merchantName} onComplete={() => setView('dashboard')} />
  return <LoginPortal onRegister={() => setView('registration')} onLogin={(name) => { setMerchantName(name); setView('welcome') }} />
}

function WelcomeTransition({ merchantName, onComplete }) {
  useEffect(() => {
    const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    const timer = window.setTimeout(onComplete, reducedMotion ? 50 : 1900)
    return () => window.clearTimeout(timer)
  }, [onComplete])

  return <main className="welcome-transition" aria-label="Welcome to your Foodie dashboard">
    <div className="welcome-glow welcome-glow-red" /><div className="welcome-glow welcome-glow-yellow" />
    <div className="welcome-transition-content"><div className="welcome-transition-mark"><img src={foodieLogo} alt="" /></div>
      <span className="welcome-transition-kicker">FOODIE MERCHANT</span><h1>Welcome back<span>.</span></h1>
      <p>{merchantName ? `Let's get your workspace ready, ${merchantName}.` : 'Let’s get your workspace ready.'}</p>
      <div className="welcome-progress"><i /></div>
    </div>
  </main>
}

function Brand({ light = false }) {
  return <a className={`brand ${light ? 'brand-light' : ''}`} href="#" onClick={(event) => event.preventDefault()} aria-label="Foodie merchant portal">
    <img className="brand-logo" src={foodieLogo} alt="" />
    <span>foodie<span className="brand-dot">.</span></span><span className="brand-tag">MERCHANT</span>
  </a>
}

function SplashScreen() {
  return <main className="splash-screen" aria-label="Loading Foodie merchant portal">
    <div className="splash-orbit splash-orbit-one" /><div className="splash-orbit splash-orbit-two" />
    <div className="splash-content"><div className="splash-mark"><img src={foodieLogo} alt="" /></div>
      <div className="splash-wordmark">foodie<span>.</span></div><p>GOOD FOOD. GREAT BUSINESS.</p>
      <div className="splash-loader" role="status"><span /><span /><span /></div><span className="splash-loading-text">PREPARING YOUR TABLE</span>
    </div>
  </main>
}

function LoginPortal({ onRegister, onLogin }) {
  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function submit(event) {
    event.preventDefault(); setError('')
    if (!identifier.trim() || !password) { setError('Enter your email or account ID and password to continue.'); return }
    setLoading(true)
    window.setTimeout(() => { onLogin(identifier.includes('@') ? identifier.split('@')[0] : identifier); setLoading(false) }, 650)
  }

  return <main className="portal-page">
    <header className="topbar"><Brand /><div className="topbar-login"><span>New to Foodie?</span><button className="login-link" type="button" onClick={onRegister}>Create an account</button></div></header>
    <section className="portal-layout">
      <aside className="portal-story"><div className="story-copy"><span className="eyebrow"><span className="eyebrow-dot" /> THE MERCHANT TABLE</span>
        <h1>YOUR NEXT<br />GREAT DAY<br /><span>STARTS HERE.</span></h1>
        <p>Orders, customers, and your restaurant's growth. Everything you need, all in one place.</p>
        <div className="story-stats"><div><strong>ONE</strong><span>simple dashboard</span></div><i /><div><strong>MORE</strong><span>ways to grow</span></div></div>
      </div><div className="story-food-art" aria-hidden="true"><span className="art-spark">✳</span><div className="art-plate"><div className="art-burger"><i /><i /><i /><i /><i /></div></div><span className="art-caption">MADE FOR<br />YOUR BUSINESS</span></div>
      <div className="story-footer"><span>GOOD FOOD. GREAT BUSINESS.</span><span>FOODIE © 2026</span></div></aside>
      <section className="login-card"><div className="login-card-top"><span className="form-label">MERCHANT PORTAL</span><span className="secure-pill"><i /> SECURE SIGN IN</span></div>
        <div className="form-heading"><h2>Welcome<br />back<span className="brand-dot">.</span></h2><p>Sign in to pick up where your business left off.</p></div>
        {error && <div className="form-message" role="alert">{error}</div>}
        <form onSubmit={submit}>
          <label className="field"><span>Email or account ID <b>*</b></span><input value={identifier} onChange={(event) => setIdentifier(event.target.value)} placeholder="you@restaurant.com" autoComplete="username" required /></label>
          <label className="field"><span>Password <b>*</b></span><div className="password-wrap"><input type={showPassword ? 'text' : 'password'} value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Enter your password" autoComplete="current-password" required /><button type="button" className="password-toggle" onClick={() => setShowPassword((value) => !value)} aria-label={showPassword ? 'Hide password' : 'Show password'}>{showPassword ? 'Hide' : 'Show'}</button></div></label>
          <div className="login-options"><span><i /> Your workspace is ready</span><button type="button" onClick={() => setError('Password recovery will be available when account services are connected.')}>Forgot password?</button></div>
          <button className="submit-button" type="submit" disabled={loading}>{loading ? <><span className="button-spinner" /> Signing in…</> : <>SIGN IN TO DASHBOARD <span aria-hidden="true">→</span></>}</button>
        </form>
        <div className="login-divider"><span>NEW TO FOODIE?</span></div>
        <button className="register-cta" type="button" onClick={onRegister}>Register your restaurant <span aria-hidden="true">↗</span></button>
      </section>
    </section>
  </main>
}

function MerchantDashboard({ merchantName, onSignOut }) {
  const [active, setActive] = useState('Home')
  const [chartMetric, setChartMetric] = useState('Sales')
  const [dateRange, setDateRange] = useState('Last 7 days')
  const navItems = ['Home', 'Orders', 'Menu', 'Wallet', 'Delivery', 'Reports', 'Payments', 'Marketing', 'Help']
  const iconMap = { Home: '⌂', Orders: '▤', Menu: '☷', Wallet: '◉', Delivery: '↗', Reports: '▥', Payments: '＄', Marketing: '✳', Help: '?' }

  return <main className="dashboard-shell">
    <aside className="dashboard-sidebar">
      <Brand light />
      <span className="sidebar-caption">WORKSPACE</span>
      <nav className="dashboard-nav" aria-label="Merchant dashboard">
        {navItems.map((item) => <button key={item} type="button" className={active === item ? 'active' : ''} onClick={() => setActive(item)}><span className="nav-icon" aria-hidden="true">{iconMap[item]}</span>{item}{item === 'Orders' && <i className="nav-indicator" />}</button>)}
      </nav>
      <div className="sidebar-bottom"><button type="button" className={active === 'Settings' ? 'active' : ''} onClick={() => setActive('Settings')}><span className="nav-icon">⚙</span>Settings</button><button type="button" onClick={onSignOut}><span className="nav-icon">↪</span>Log Out</button><div className="sidebar-profile"><span className="profile-avatar">{(merchantName || 'M').slice(0, 1).toUpperCase()}</span><span><b>{merchantName || 'Merchant'}</b><small>Merchant account</small></span><i>···</i></div></div>
    </aside>
    <section className="dashboard-main">
      <header className="dashboard-topbar"><div className="breadcrumb">Workspace <span>/</span> {active}</div><div className="dashboard-top-actions"><button className="icon-button" aria-label="Notifications" type="button">♧<i /></button><span className="topbar-divider" /><span className="top-avatar">{(merchantName || 'M').slice(0, 1).toUpperCase()}</span></div></header>
      {active === 'Home' ? <div className="dashboard-content">
        <div className="dashboard-heading-row"><div><span className="dashboard-kicker">YOUR BUSINESS AT A GLANCE</span><h1>Good afternoon{merchantName ? `, ${merchantName}` : ''}<span>.</span></h1><p>Here’s what’s happening with your restaurant.</p></div><button className="date-select" type="button" onClick={() => setDateRange((range) => range === 'Last 7 days' ? 'Last 30 days' : 'Last 7 days')}><span>▦</span>{dateRange}<span className="date-chevron">⌄</span></button></div>
        <div className="metric-grid">{[['Total Sales', '↗', 'red'], ['Total Orders', '▤', 'yellow'], ['Average Order Value', '◉', 'peach'], ['Total Customers', '♙', 'mint']].map(([label, icon, tone]) => <article className="metric-card" key={label}><div className="metric-top"><span>{label}</span><i className={`metric-icon ${tone}`}>{icon}</i></div><strong className="metric-empty">—</strong><small>Data will appear when available</small></article>)}</div>
        <section className="dashboard-card sales-card"><div className="card-heading"><div><h2>Business performance</h2><p>Track your restaurant over time</p></div><div className="chart-toggles" role="group" aria-label="Chart metric">{['Sales', 'Orders', 'Average Order Value'].map((metric) => <button type="button" key={metric} className={chartMetric === metric ? 'selected' : ''} onClick={() => setChartMetric(metric)}>{metric}</button>)}</div></div><div className="chart-empty"><div className="chart-y-axis"><span>—</span><span>—</span><span>—</span><span>—</span></div><div className="chart-grid-lines"><i /><i /><i /><i /><div className="chart-empty-message"><span>⌁</span><b>No {chartMetric.toLowerCase()} data yet</b><small>Performance data will appear here once available.</small></div></div></div><div className="chart-x-axis"><span>Data unavailable</span></div></section>
        <div className="insights-grid"><section className="dashboard-card insight-card"><div className="card-heading"><div><h2>Top Eats</h2><p>What customers are enjoying</p></div><span className="card-spark">✳</span></div><EmptyState title="Nothing to show yet" detail="Your popular picks will appear here." /></section>
          <section className="dashboard-card insight-card"><div className="card-heading"><div><h2>Top Selling Items</h2><p>Your best performers</p></div><button type="button" className="subtle-link" onClick={() => setActive('Menu')}>View menu →</button></div><EmptyState title="No sales to rank yet" detail="Item performance will appear here." /></section>
          <section className="dashboard-card insight-card"><div className="card-heading"><div><h2>Customers</h2><p>New and repeat visitors</p></div></div><div className="customer-legend"><span><i className="legend-new" />New customers</span><span><i className="legend-repeat" />Repeat customers</span></div><div className="customer-empty"><div className="donut-empty"><span>—</span></div><p>Customer insights will appear here when available.</p></div></section>
          <section className="dashboard-card insight-card feedback-card"><div className="card-heading"><div><h2>Menu Item Feedback</h2><p>Hear what customers think</p></div><span className="feedback-stars">★★★★★</span></div><EmptyState title="No feedback yet" detail="Customer feedback will appear here." /></section>
        </div>
      </div> : <div className="dashboard-content section-content"><span className="dashboard-kicker">FOODIE MERCHANT WORKSPACE</span><h1>{active}<span>.</span></h1><p>Manage your {active.toLowerCase()} workspace here.</p><section className="dashboard-card section-empty"><EmptyState title={`${active} data will appear here`} detail="There’s nothing to show right now." /></section></div>}
    </section>
  </main>
}

function EmptyState({ title, detail }) {
  return <div className="empty-state"><span className="empty-state-mark">⌁</span><b>{title}</b><p>{detail}</p></div>
}

function RegistrationPortal({ onLogin }) {
  const [showPassword, setShowPassword] = useState(false)
  const [frontPreview, setFrontPreview] = useState('')
  const [backPreview, setBackPreview] = useState('')
  const [message, setMessage] = useState('')

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    nationalId: '',
    email: '',
    restaurantName: '',
    restaurantAddress: '',
    restaurantPhone: '',
    password: '',
    confirmPassword: '',
  })

  function handleChange(event) {
    const { name, value } = event.target

    setFormData((previous) => ({
      ...previous,
      [name]: value,
    }))
  }

  function handleFileChange(event, side) {
    const file = event.target.files?.[0]

    if (!file) return

    if (!file.type.startsWith('image/')) {
      setMessage('Please select an image file for the document.')
      event.target.value = ''
      return
    }

    if (file.size > 5 * 1024 * 1024) {
      setMessage('Each document image must be 5 MB or smaller.')
      event.target.value = ''
      return
    }

    setMessage('')

    const previewUrl = URL.createObjectURL(file)

    if (side === 'front') {
      setFrontPreview((previous) => {
        if (previous) URL.revokeObjectURL(previous)
        return previewUrl
      })
    } else {
      setBackPreview((previous) => {
        if (previous) URL.revokeObjectURL(previous)
        return previewUrl
      })
    }
  }

  function handleSubmit(event) {
    event.preventDefault()
    setMessage('')

    if (formData.password.length < 8) {
      setMessage('Your password must contain at least 8 characters.')
      return
    }

    if (formData.password !== formData.confirmPassword) {
      setMessage('Your passwords do not match.')
      return
    }

    if (!frontPreview || !backPreview) {
      setMessage('Please upload both sides of your business registration.')
      return
    }

    setMessage(
        'Your information is ready. Account services are not connected yet.'
    )
  }

  return (
      <main className="merchant-page">
        <header className="topbar">
          <a className="brand" href="#" aria-label="Foodie home">
            <img
                className="brand-logo"
                src="https://res.cloudinary.com/g7u1tdxx/image/upload/v1792826258/Gemini_Generated_Image_v0q9c4v0q9c4v0q9.jpg"
                alt="Foodie logo"
            />
            <span>foodie<span className="brand-dot">.</span></span>
            <span className="brand-tag">MERCHANT</span>
          </a>

          <div className="topbar-login">
            <span>Already a partner?</span>
            <button
                className="login-link"
                type="button"
                onClick={onLogin}
            >
              Sign in
            </button>
          </div>
        </header>

        <section className="registration-layout">
          <aside className="welcome-panel">
            <div className="welcome-content">
            <span className="eyebrow">
              <span className="eyebrow-dot" />
              GROW WITH FOODIE
            </span>

              <h1>
                Your food.
                <br />
                Your business.
                <br />
                <span>MORE POSSIBILITIES.</span>
              </h1>

              <p className="welcome-description">
                Bring your restaurant online, connect with hungry customers,
                and manage your business from one place.
              </p>

              <div className="benefit-list">
                <div className="benefit">
                  <span className="benefit-icon">01</span>
                  <div>
                    <strong>REACH MORE CUSTOMERS</strong>
                    <p>Make your menu available online.</p>
                  </div>
                </div>

                <div className="benefit">
                  <span className="benefit-icon">02</span>
                  <div>
                    <strong>MANAGE ORDERS EASILY</strong>
                    <p>Keep your restaurant organized.</p>
                  </div>
                </div>

                <div className="benefit">
                  <span className="benefit-icon">03</span>
                  <div>
                    <strong>GROW YOUR BUSINESS</strong>
                    <p>Track your restaurant's performance.</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="welcome-footer">
              <span>GOOD FOOD. GREAT BUSINESS.</span>
              <span>FOODIE © 2026</span>
            </div>
          </aside>

          <section className="form-panel">
            <div className="form-heading">
              <span className="form-label">MERCHANT REGISTRATION</span>
              <h2>Let's get you started.</h2>
              <p>
                Tell us about yourself and your restaurant to create your
                merchant account.
              </p>
            </div>

            {message && (
                <div className="form-message" role="status" aria-live="polite">
                  {message}
                </div>
            )}

            <form onSubmit={handleSubmit}>
              <section className="form-section">
                <div className="section-heading">
                  <span className="section-number">01</span>
                  <div>
                    <h3>Owner information</h3>
                    <p>Your personal and account details</p>
                  </div>
                </div>

                <div className="field-grid">
                  <label className="field">
                    <span>First name <b>*</b></span>
                    <input
                        name="firstName"
                        value={formData.firstName}
                        onChange={handleChange}
                        placeholder="Enter first name"
                        autoComplete="given-name"
                        required
                    />
                  </label>

                  <label className="field">
                    <span>Last name <b>*</b></span>
                    <input
                        name="lastName"
                        value={formData.lastName}
                        onChange={handleChange}
                        placeholder="Enter last name"
                        autoComplete="family-name"
                        required
                    />
                  </label>

                  <label className="field">
                    <span>National ID number <b>*</b></span>
                    <input
                        name="nationalId"
                        value={formData.nationalId}
                        onChange={handleChange}
                        placeholder="Enter your national ID"
                        autoComplete="off"
                        required
                    />
                  </label>

                  <label className="field">
                    <span>Email address <b>*</b></span>
                    <input
                        name="email"
                        type="email"
                        value={formData.email}
                        onChange={handleChange}
                        placeholder="you@example.com"
                        autoComplete="email"
                        required
                    />
                  </label>
                </div>
              </section>

              <section className="form-section">
                <div className="section-heading">
                  <span className="section-number">02</span>
                  <div>
                    <h3>Restaurant information</h3>
                    <p>Tell us about your business</p>
                  </div>
                </div>

                <div className="field-grid">
                  <label className="field full-width">
                    <span>Restaurant name <b>*</b></span>
                    <input
                        name="restaurantName"
                        value={formData.restaurantName}
                        onChange={handleChange}
                        placeholder="e.g. The Hungry Kitchen"
                        autoComplete="organization"
                        required
                    />
                  </label>

                  <label className="field full-width">
                    <span>Restaurant address <b>*</b></span>
                    <textarea
                        name="restaurantAddress"
                        value={formData.restaurantAddress}
                        onChange={handleChange}
                        placeholder="Enter your complete restaurant address"
                        rows={3}
                        autoComplete="street-address"
                        required
                    />
                  </label>

                  <label className="field full-width">
                    <span>Restaurant contact number <b>*</b></span>
                    <input
                        name="restaurantPhone"
                        type="tel"
                        value={formData.restaurantPhone}
                        onChange={handleChange}
                        placeholder="Enter restaurant phone number"
                        autoComplete="tel"
                        required
                    />
                  </label>
                </div>
              </section>

              <section className="form-section">
                <div className="section-heading">
                  <span className="section-number">03</span>
                  <div>
                    <h3>Business registration</h3>
                    <p>Upload clear images or scans of both sides</p>
                  </div>
                </div>

                <div className="upload-grid">
                  <DocumentUpload
                      title="Front side"
                      preview={frontPreview}
                      inputId="business-front"
                      onChange={(event) => handleFileChange(event, 'front')}
                  />

                  <DocumentUpload
                      title="Back side"
                      preview={backPreview}
                      inputId="business-back"
                      onChange={(event) => handleFileChange(event, 'back')}
                  />
                </div>

                <p className="upload-note">
                  JPG, PNG or other browser-supported image formats · Maximum
                  5 MB per image. Scan or photograph each side clearly.
                </p>
              </section>

              <section className="form-section">
                <div className="section-heading">
                  <span className="section-number">04</span>
                  <div>
                    <h3>Secure your account</h3>
                    <p>Create the password you'll use to sign in</p>
                  </div>
                </div>

                <div className="field-grid">
                  <label className="field full-width">
                    <span>Password <b>*</b></span>
                    <div className="password-wrap">
                      <input
                          name="password"
                          type={showPassword ? 'text' : 'password'}
                          value={formData.password}
                          onChange={handleChange}
                          placeholder="At least 8 characters"
                          autoComplete="new-password"
                          minLength={8}
                          required
                      />
                      <button
                          type="button"
                          className="password-toggle"
                          onClick={() => setShowPassword(!showPassword)}
                          aria-label={showPassword ? 'Hide password' : 'Show password'}
                      >
                        {showPassword ? 'Hide' : 'Show'}
                      </button>
                    </div>
                  </label>

                  <label className="field full-width">
                    <span>Confirm password <b>*</b></span>
                    <input
                        name="confirmPassword"
                        type={showPassword ? 'text' : 'password'}
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        placeholder="Enter your password again"
                        autoComplete="new-password"
                        minLength={8}
                        required
                    />
                  </label>
                </div>

                <p className="security-note">
                  <span aria-hidden="true">🔒</span>
                  Your account credentials and documents must be securely
                  processed by the Foodie backend.
                </p>
              </section>

              <button className="submit-button" type="submit">
                Create merchant account <span aria-hidden="true">→</span>
              </button>

              <p className="terms-note">
                By continuing, you agree to Foodie's merchant registration
                terms and verification process.
              </p>
            </form>
          </section>
        </section>
      </main>
  )
}

function DocumentUpload({ title, preview, inputId, onChange }) {
  return (
      <div className="upload-card">
        <label className="upload-dropzone" htmlFor={inputId}>
          {preview ? (
              <img className="document-preview" src={preview} alt={`${title} preview`} />
          ) : (
              <>
                <span className="upload-icon" aria-hidden="true">↑</span>
                <strong>Upload {title.toLowerCase()}</strong>
                <span>Choose an image or scan</span>
              </>
          )}
        </label>

        <div className="upload-card-footer">
          <span>{title}</span>
          <label className="browse-button" htmlFor={inputId}>
            {preview ? 'Change image' : 'Browse'}
          </label>
        </div>

        <input
            id={inputId}
            type="file"
            accept="image/*"
            onChange={onChange}
            className="file-input"
            required
        />
      </div>
  )
}

export default App
