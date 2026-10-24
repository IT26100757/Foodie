
import { useState } from 'react'
import './App.css'

function App() {
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
        'Demo form validated! Registration is not submitted or saved yet. Backend integration is required.'
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
                onClick={() =>
                    setMessage('Merchant login will be implemented in the next step.')
                }
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
                <span>More possibilities.</span>
              </h1>

              <p className="welcome-description">
                Bring your restaurant online, connect with hungry customers,
                and manage your business from one place.
              </p>

              <div className="benefit-list">
                <div className="benefit">
                  <span className="benefit-icon">01</span>
                  <div>
                    <strong>Reach more customers</strong>
                    <p>Make your menu available online.</p>
                  </div>
                </div>

                <div className="benefit">
                  <span className="benefit-icon">02</span>
                  <div>
                    <strong>Manage orders easily</strong>
                    <p>Keep your restaurant organized.</p>
                  </div>
                </div>

                <div className="benefit">
                  <span className="benefit-icon">03</span>
                  <div>
                    <strong>Grow your business</strong>
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
