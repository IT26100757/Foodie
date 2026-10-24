export function playOrderAlert() {
  try {
    const AudioContextClass = window.AudioContext || window.webkitAudioContext
    if (!AudioContextClass) return
    const context = new AudioContextClass()
    const now = context.currentTime
    ;[660, 880].forEach((frequency, index) => {
      const oscillator = context.createOscillator()
      const gain = context.createGain()
      oscillator.type = 'sine'
      oscillator.frequency.value = frequency
      gain.gain.setValueAtTime(0.0001, now + index * 0.13)
      gain.gain.exponentialRampToValueAtTime(0.16, now + index * 0.13 + 0.025)
      gain.gain.exponentialRampToValueAtTime(0.0001, now + index * 0.13 + 0.19)
      oscillator.connect(gain)
      gain.connect(context.destination)
      oscillator.start(now + index * 0.13)
      oscillator.stop(now + index * 0.13 + 0.2)
    })
    window.setTimeout(() => context.close(), 600)
  } catch { /* Sound is optional when unsupported or blocked by the browser. */ }
}