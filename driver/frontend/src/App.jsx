import { useEffect, useState } from 'react'
import './App.css'

const steps = ['Personal Information', 'Contact Details', 'Identity Documents', 'Vehicle Documents', 'Vehicle Photos']

function Icon({ name, size = 18 }) {
  const shapes = {
    user: <><circle cx="12" cy="8" r="3.4"/><path d="M5 20c.6-3.3 3.1-5.2 7-5.2s6.4 1.9 7 5.2"/></>,
    eye: <><path d="M2.5 12s3.2-6 9.5-6 9.5 6 9.5 6-3.2 6-9.5 6-9.5-6-9.5-6Z"/><circle cx="12" cy="12" r="2.5"/></>,
    eyeOff: <><path d="m3 3 18 18M10.6 6.2A10.5 10.5 0 0 1 12 6c6.3 0 9.5 6 9.5 6a15 15 0 0 1-3 3.7M6.2 6.8C3.7 8.4 2.5 12 2.5 12s3.2 6 9.5 6c.8 0 1.5-.1 2.2-.3"/><path d="M9.9 9.9a3 3 0 0 0 4.2 4.2"/></>,
    calendar: <><rect x="3.5" y="5" width="17" height="16" rx="2"/><path d="M7.5 3v4M16.5 3v4M3.5 9.5h17M8 13h2m4 0h2M8 17h2m4 0h2"/></>,
    chevronDown: <path d="m7 10 5 5 5-5"/>,
    chevronLeft: <path d="m14.5 5-7 7 7 7"/>,
    chevronRight: <path d="m9.5 5 7 7-7 7"/>,
    arrowLeft: <><path d="M19 12H5"/><path d="m11 18-6-6 6-6"/></>,
    arrowRight: <><path d="M5 12h14"/><path d="m13 6 6 6-6 6"/></>,
    phone: <path d="M7 3H4.8A1.8 1.8 0 0 0 3 4.9C3.8 13.5 10.5 20.2 19.1 21a1.8 1.8 0 0 0 1.9-1.8V17l-4.6-2-2.1 2.1a15 15 0 0 1-6.5-6.5L9.9 8.5 7 3Z"/>,
    upload: <><path d="M12 15V3m0 0L7.5 7.5M12 3l4.5 4.5"/><path d="M5 13v6a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-6"/></>,
    check: <path d="m5 12.5 4.2 4.2L19.5 6.8"/>,
    info: <><circle cx="12" cy="12" r="9"/><path d="M12 11v5m0-8h.01"/></>,
  }
  return <svg className="ui-icon" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{shapes[name]}</svg>
}

function Scooter({ small = false }) {
  return <div className={small ? 'scooter-art mini-scooter' : 'scooter-art'} role="img" aria-label="Food delivery rider riding an orange scooter"><img src="/rider-scooter-v2.png" alt=""/><span className="spin-wheel rear"><i/></span><span className="spin-wheel front"><i/></span></div>
}

function Brand({ compact = false, splash = false }) { return <div className={compact ? 'brand-lockup compact' : `brand-lockup${splash ? ' splash-brand' : ''}`}><div className="brand-mark"><span className="brand-speed">〰</span><span className="brand-cloche">⌒</span>foodie<span className="brand-period">.</span></div>{compact ? <small>DRIVER</small> : splash ? <div className="portal-label">DRIVER</div> : <><div className="portal-label">DRIVER PORTAL</div><p>Good food. Brighter journeys.</p></>}</div> }

function UploadBox({ label, accept = 'image/*,.pdf', hint = 'JPG, PNG or PDF (Max 5MB)', required = true }) {
  const [file, setFile] = useState(null)
  return <label className="upload-field"><span className="upload-title">{label}{required && <b> *</b>}</span><span className={`upload-box ${file ? 'has-file' : ''}`}><span className="upload-icon"><Icon name={file ? 'check' : 'upload'} size={22}/></span><span>{file ? file.name : 'Upload file'}</span><input type="file" accept={accept} required={required} onChange={e => setFile(e.target.files?.[0] || null)} /></span><small>{file ? `${(file.size / 1024 / 1024).toFixed(1)} MB · ready` : hint}</small></label>
}

function Field({ label, placeholder, type = 'text', required = true, ...props }) {
  const nameField = label === 'First Name' || label === 'Last Name'
  const onChange = nameField ? event => {
    const cleaned = event.target.value.replace(/[^A-Za-zÀ-ÖØ-öø-ÿ'’ -]/g, '')
    if (cleaned !== event.target.value) event.target.value = cleaned
    props.onChange?.(event)
  } : props.onChange
  return <label className="field"><span>{label}{required && <b> *</b>}</span><input type={type} placeholder={placeholder} required={required} {...props} onChange={onChange} pattern={nameField ? "[A-Za-zÀ-ÖØ-öø-ÿ'’ -]+" : props.pattern} /></label>
}

function DatePicker({ value, onChange, error }) {
  const [open, setOpen] = useState(false)
  const [draft, setDraft] = useState(null)
  const [viewDate, setViewDate] = useState(() => new Date())
  const [yearPicker, setYearPicker] = useState(false)
  const today = new Date()
  const firstDay = new Date(viewDate.getFullYear(), viewDate.getMonth(), 1).getDay()
  const daysInMonth = new Date(viewDate.getFullYear(), viewDate.getMonth() + 1, 0).getDate()
  const calendarDays = Array.from({ length: 42 }, (_, index) => new Date(viewDate.getFullYear(), viewDate.getMonth(), index - firstDay + 1))
  const formatValue = date => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  const sameDate = (a, b) => a && b && formatValue(a) === formatValue(b)
  const openPicker = () => {
    setDraft(value ? new Date(`${value}T00:00:00`) : null)
    if (value) setViewDate(new Date(`${value}T00:00:00`))
    setYearPicker(false)
    setOpen(true)
  }
  const chooseDate = date => { setDraft(date); setViewDate(new Date(date.getFullYear(), date.getMonth(), 1)) }
  const confirmDate = () => { if (draft) onChange(formatValue(draft)); setOpen(false) }
  const selectedText = date => date ? date.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' }) : 'Choose your birthday'

  return <div className="date-field field"><span>Date of Birth <b>*</b></span><input type="hidden" name="birthDate" value={value}/><div className="date-input-wrap"><input type="text" value={value ? new Date(`${value}T00:00:00`).toLocaleDateString('en-GB') : ''} placeholder="dd / mm / yyyy" readOnly onClick={openPicker} onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); openPicker() } }} aria-haspopup="dialog" aria-required="true"/><button type="button" aria-label="Choose date of birth" onClick={openPicker}><Icon name="calendar"/></button></div>{error && <em className="date-error">{error}</em>}
    {open && <div className="date-overlay" onMouseDown={e => { if (e.target === e.currentTarget) setOpen(false) }}><section className="date-dialog" role="dialog" aria-modal="true" aria-label="Choose date of birth"><div className="date-dialog-top"><span>Select date</span><strong>{selectedText(draft)}</strong></div><div className="calendar-toolbar">{yearPicker ? <span className="calendar-caption">Choose year</span> : <button type="button" className="calendar-caption" onClick={() => setYearPicker(true)}>{viewDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })} <Icon name="chevronDown" size={16}/></button>}<div className="calendar-arrows"><button type="button" aria-label="Previous month" onClick={() => setViewDate(new Date(viewDate.getFullYear(), viewDate.getMonth() - 1, 1))}><Icon name="chevronLeft"/></button><button type="button" aria-label="Next month" disabled={viewDate.getFullYear() >= today.getFullYear() && viewDate.getMonth() >= today.getMonth()} onClick={() => setViewDate(new Date(viewDate.getFullYear(), viewDate.getMonth() + 1, 1))}><Icon name="chevronRight"/></button></div></div>
      {yearPicker ? <div className="year-grid">{Array.from({ length: today.getFullYear() - 1939 }, (_, i) => today.getFullYear() - i).map(year => <button type="button" key={year} className={year === viewDate.getFullYear() ? 'chosen' : ''} onClick={() => { setViewDate(new Date(year, viewDate.getMonth(), 1)); setYearPicker(false) }}>{year}</button>)}</div> : <><div className="weekday-row">{['S','M','T','W','T','F','S'].map((day, i) => <span key={`${day}${i}`}>{day}</span>)}</div><div className="calendar-days">{calendarDays.map((date, i) => { const currentMonth = date.getMonth() === viewDate.getMonth(); const future = date > new Date(today.getFullYear(), today.getMonth(), today.getDate()); return <button type="button" key={`${formatValue(date)}-${i}`} disabled={future} className={`${currentMonth ? '' : 'outside-month'} ${sameDate(date, draft) ? 'chosen' : ''}`} onClick={() => chooseDate(date)}>{date.getDate()}</button> })}</div></>}
      <div className="date-dialog-actions"><button type="button" onClick={() => setOpen(false)}>Cancel</button><button type="button" className="date-confirm" disabled={!draft} onClick={confirmDate}>OK</button></div>
    </section></div>}
  </div>
}

function App() {
  const [page, setPage] = useState('splash')
  const [step, setStep] = useState(0)
  const [phone, setPhone] = useState('')
  const [phoneError, setPhoneError] = useState('')
  const [birthDate, setBirthDate] = useState('')
  const [birthDateError, setBirthDateError] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [remember, setRemember] = useState(false)

  useEffect(() => {
    const timer = setTimeout(() => setPage(localStorage.getItem('foodie-driver-demo-registered') === 'true' ? 'dashboard' : 'login'), 3600)
    return () => clearTimeout(timer)
  }, [])

  const normalizePhone = value => value.replace(/[^\d+]/g, '')
  const validPhone = value => /^(?:\+94|0)?\d{9}$/.test(normalizePhone(value))
  const next = e => {
    e.preventDefault()
    if (step === 0 && !birthDate) { setBirthDateError('Select your date of birth.'); return }
    setBirthDateError('')
    if (step === 1 && !validPhone(phone)) { setPhoneError('Enter a valid Sri Lankan phone number, for example +94 55 756 8643.'); return }
    setPhoneError('')
    if (step < 4) setStep(step + 1)
    else { localStorage.setItem('foodie-driver-demo-registered', 'true'); setPage('submitted') }
  }

  return <main className="portal-shell">
    {page === 'splash' && <section className="splash-screen"><div className="splash-sun"/><div className="splash-cloud cloud-a"/><div className="splash-cloud cloud-b"/><Brand splash/><div className="splash-city"/><div className="splash-road"/><div className="splash-streak streak-a"/><div className="splash-streak streak-b"/><div className="splash-puff"><i/><i/><i/></div><div className="splash-ride"><Scooter/></div></section>}
    {page === 'login' && <section className="login-layout"><div className="login-art"><div className="art-cloud one"/><div className="art-cloud two"/><div className="art-city"/><Brand/><div className="sun-disc"/><div className="login-rider"><Scooter small/></div></div><div className="login-card"><h1>Welcome Back</h1><p className="subhead">Log in to your driver account</p><form onSubmit={e => { e.preventDefault(); setPage('dashboard') }}><Field label="Email Address" placeholder="Enter your email" type="email"/><label className="field"><span>Password</span><div className="password-wrap"><input type={showPassword ? 'text' : 'password'} placeholder="Enter your password" required/><button type="button" className="eye" aria-label={showPassword ? 'Hide password' : 'Show password'} onClick={() => setShowPassword(!showPassword)}><Icon name={showPassword ? 'eyeOff' : 'eye'} size={19}/></button></div></label><div className="login-options"><label className="remember"><input type="checkbox" checked={remember} onChange={e => setRemember(e.target.checked)}/> Remember me</label><button type="button" className="text-button">Forgot password?</button></div><button className="primary full">Login</button></form><div className="or-line"><span/>or<span/></div><button className="outline full driver-button" onClick={() => {setPage('register');setStep(0)}}><Icon name="user" size={18}/> I'm a new driver</button><p className="register-prompt">Don't have an account? <button className="text-button" onClick={() => {setPage('register');setStep(0)}}>Register here</button></p><p className="demo-note">Frontend demo · Authentication will connect to the driver API</p></div></section>}
    {page === 'register' && <section className="register-layout"><aside className="step-sidebar"><Brand compact/><div className="step-count">Step {step + 1} of 5</div><nav>{steps.map((title, i) => <button key={title} onClick={() => i < step && setStep(i)} className={`step-nav ${i === step ? 'active' : ''} ${i < step ? 'done' : ''}`}><span>{i < step ? <Icon name="check" size={15}/> : i + 1}</span>{title}</button>)}</nav><div className="sidebar-city"/><div className="sidebar-rider"><Scooter small/></div></aside><div className="form-panel"><div className="registration-scenery" aria-hidden="true"><i className="scenery-cloud cloud-one"/><i className="scenery-cloud cloud-two"/><i className="scenery-sun"/><i className="scenery-skyline"/><i className="scenery-tree tree-one"/><i className="scenery-tree tree-two"/><i className="scenery-road"/></div><div className="step-progress"><i style={{width:`${(step + 1) * 20}%`}}/></div><div className="step-heading"><h1>{steps[step]}</h1><p>{['Let’s start with your basic details','Where can we reach you?','Upload clear photos or scans of your documents','Upload your vehicle related documents','Upload clear photos of your vehicle'][step]}</p></div><form className="step-form" onSubmit={next}>
      {step === 0 && <div className="field-grid"><Field label="First Name" placeholder="Enter your first name"/><Field label="Last Name" placeholder="Enter your last name"/><Field label="NIC Number" placeholder="Enter your NIC number"/><Field label="Driving License Number" placeholder="Enter your driving license number"/><DatePicker value={birthDate} onChange={date => {setBirthDate(date);setBirthDateError('')}} error={birthDateError}/></div>}
      {step === 1 && <div className="field-grid contact-grid"><Field label="Email Address" placeholder="Enter your email address" type="email"/><label className="field"><span>Contact Number <b>*</b></span><div className="phone-wrap"><span><Icon name="phone" size={16}/> +94 <Icon name="chevronDown" size={14}/></span><input type="tel" value={phone} onChange={e => {setPhone(e.target.value);setPhoneError('')}} placeholder="55 756 8643" required/></div><small>Please enter a valid Sri Lankan phone number (e.g. +94 55 756 8643)</small>{phoneError && <em>{phoneError}</em>}</label><Field label="Address Line 1" placeholder="Street address, house number"/><Field label="Address Line 2" placeholder="Apartment, building, etc. (optional)" required={false}/><Field label="Address Line 3" placeholder="City, district, postal code" required={false}/></div>}
      {step === 2 && <div className="upload-grid"><UploadBox label="NIC (Front Side)"/><UploadBox label="NIC (Back Side)"/><UploadBox label="Driving License (Front Side)"/><UploadBox label="Driving License (Back Side)"/></div>}
      {step === 3 && <div className="vehicle-docs"><div className="vehicle-group"><Field label="Vehicle Registration Number" placeholder="Enter vehicle registration number"/><div className="upload-grid"><UploadBox label="Vehicle Registration Document (Front Side)"/><UploadBox label="Vehicle Registration Document (Back Side)"/></div><p className="upload-hint">You can upload a PDF document or clear photos of both sides.</p></div><div className="vehicle-group"><div className="upload-grid"><UploadBox label="Vehicle Revenue Licence (Front Side)"/><UploadBox label="Vehicle Revenue Licence (Back Side)"/></div></div><div className="vehicle-group"><div className="upload-grid"><UploadBox label="Vehicle Insurance (Front Side)"/><UploadBox label="Vehicle Insurance (Back Side)"/></div></div></div>}
      {step === 4 && <><div className="upload-grid three-uploads"><UploadBox label="Front View" accept="image/*" hint="JPG or PNG (Max 5MB)"/><UploadBox label="Back View" accept="image/*" hint="JPG or PNG (Max 5MB)"/><UploadBox label="Side View" accept="image/*" hint="JPG or PNG (Max 5MB)"/></div><div className="guidelines"><strong><Icon name="info" size={17}/> Important Guidelines:</strong><ul><li>Upload clear and recent photos</li><li>Show the full vehicle in each photo</li><li>Make sure the number plate is visible</li><li>JPG or PNG format (Max 5MB each)</li></ul></div></>}
      <div className="form-actions">{step > 0 ? <button type="button" className="back-button" onClick={() => setStep(step - 1)}><Icon name="arrowLeft" size={17}/> Back</button> : <button type="button" className="back-button" onClick={() => setPage('login')}>Cancel</button>}{step < 4 ? <button type="submit" className="primary next-button">Next <Icon name="arrowRight" size={17}/></button> : <button type="submit" className="primary">Submit Registration</button>}</div>
    </form></div></section>}
    {(page === 'dashboard' || page === 'submitted') && <section className="demo-dashboard"><Brand/><div className="dashboard-card"><span className="success-mark"><Icon name="check" size={27}/></span><h1>{page === 'submitted' ? 'Registration submitted' : 'Welcome to Foodie Driver'}</h1><p>{page === 'submitted' ? 'Your details are saved in this frontend demo. Connect the driver API to submit them for review.' : 'You are viewing the driver dashboard demo. Connect the driver API for real account verification.'}</p>{page === 'submitted' && <button className="primary" onClick={() => setPage('login')}>Back to Login</button>}<button className="text-button" onClick={() => {localStorage.removeItem('foodie-driver-demo-registered');setPage('login')}}>Sign out of demo</button></div></section>}
  </main>
}

export default App
