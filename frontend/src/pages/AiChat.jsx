import { useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import toast from 'react-hot-toast'
import client from '../api/client'
import { errorMessage } from '../utils/status'
import { PageHeader } from '../components/ui'

const sparks = [
  'What rooms are available now?',
  'Find a classroom for 50 students tomorrow from 2–4 PM with a projector.',
  'Where is the Digital Library?',
  'Show my upcoming bookings.',
  'I need a seminar hall for 100 people tomorrow from 10 AM to 1 PM.',
]

export default function AiChat() {
  const [input, setInput] = useState(sparks[1])
  const [messages, setMessages] = useState([])
  const [recs, setRecs] = useState([])
  const [loading, setLoading] = useState(false)

  const send = async (text) => {
    const message = (text || input).trim()
    if (!message || loading) return
    setInput('')
    setMessages((m) => [...m, { role: 'user', content: message }])
    setLoading(true)
    try {
      const [{ data: chat }, { data: found }] = await Promise.all([
        client.post('/ai/chat', { message, history: messages }),
        client.post('/ai/interpret', { prompt: message }),
      ])
      setMessages((m) => [...m, { role: 'assistant', content: chat.reply, aiAvailable: chat.aiAvailable }])
      setRecs(found.recommendations || [])
    } catch (err) {
      toast.error(errorMessage(err, 'Assistant could not answer just now.'))
      setMessages((m) => [...m, { role: 'assistant', content: 'I could not reach the assistant. Use the campus map or booking wizard — both work without AI.' }])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[1.1fr_.9fr]">
      <div>
        <PageHeader kicker="Assistant" title="Ask the campus" subtitle="Answers are grounded in MySQL. Groq only writes the sentence — it cannot invent rooms." actions={<Link to="/ai-booking" className="btn-primary">Natural-language booking</Link>} />
        <div className="mb-3 flex flex-wrap gap-2">
          {sparks.map((s) => (
            <button key={s} onClick={() => send(s)} className="rounded-full border px-3 py-1 text-xs hover:bg-white dark:border-white/10">{s}</button>
          ))}
        </div>
        <div className="min-h-[360px] space-y-3">
          {messages.map((m, i) => (
            <motion.div
              key={i}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              className={`rounded-2xl p-4 text-sm ${m.role === 'user' ? 'ml-10 bg-grove-700 text-white' : 'mr-10 bg-white shadow-sm dark:bg-grove-800'}`}
            >
              {m.content}
            </motion.div>
          ))}
          {loading && <div className="mr-10 animate-pulse rounded-2xl bg-white p-4 text-sm dark:bg-grove-800">Looking through the campus database…</div>}
        </div>
        <form onSubmit={(e) => { e.preventDefault(); send() }} className="mt-4 flex gap-2">
          <input className="field flex-1 rounded-full" value={input} onChange={(e) => setInput(e.target.value)} placeholder="Describe the room you need…" />
          <button className="btn-primary" disabled={loading}>Send</button>
        </form>
      </div>
      <aside className="space-y-3">
        <div className="font-display text-2xl">Recommended from SQL</div>
        {recs.map((r) => (
          <div key={r.resourceId} className="soft-card p-4">
            <div className="font-semibold">{r.resourceName}</div>
            <div className="text-xs text-grove-500">{r.resourceCode} · score {r.score}</div>
            <p className="mt-2 text-sm">{r.reason}</p>
            <Link to={`/book/${r.resourceId}`} className="btn-primary mt-3">Book this room</Link>
          </div>
        ))}
        {recs.length === 0 && <p className="text-sm text-grove-500">Ask for a classroom, lab or hall and matching rooms will appear here.</p>}
      </aside>
    </div>
  )
}
