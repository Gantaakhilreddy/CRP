import { useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import toast from 'react-hot-toast'
import client from '../api/client'
import { errorMessage } from '../utils/status'

export default function AiBooking() {
  const [prompt, setPrompt] = useState('Find me a classroom for 50 students tomorrow from 2–4 PM with a projector.')
  const [res, setRes] = useState(null)
  const [loading, setLoading] = useState(false)

  const run = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const { data } = await client.post('/ai/interpret', { prompt })
      setRes(data)
    } catch (err) {
      toast.error(errorMessage(err, 'AI assistant is temporarily unavailable. You can continue with manual booking.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto max-w-3xl">
      <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} className="overflow-hidden rounded-[2rem] bg-grove-800 p-8 text-cream-50 shadow-xl">
        <p className="text-xs uppercase tracking-[0.25em] text-gold-400">Natural-language booking</p>
        <h1 className="mt-2 font-display text-4xl">Describe the room. We only return rooms that exist.</h1>
        <form onSubmit={run} className="mt-6">
          <textarea className="h-28 w-full rounded-2xl bg-white/10 p-4 outline-none ring-0" value={prompt} onChange={(e) => setPrompt(e.target.value)} />
          <button disabled={loading} className="mt-4 rounded-full bg-gold-500 px-5 py-2 font-semibold text-grove-900">{loading ? 'Searching campus…' : 'Find resources'}</button>
        </form>
        {res && !res.aiAvailable && (
          <p className="mt-3 text-sm text-gold-400">Groq is offline. These matches still come from SQL availability — you can book them now.</p>
        )}
      </motion.div>
      {res && (
        <div className="mt-6 space-y-3">
          <p className="text-sm text-grove-600">{res.explanation}</p>
          <p className="text-xs uppercase tracking-widest text-grove-500">
            {res.intent?.resourceType} · {res.intent?.date} · {res.intent?.startTime}–{res.intent?.endTime} · cap {res.intent?.capacity || 'any'}
          </p>
          {res.recommendations.map((r) => (
            <motion.div key={r.resourceId} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="soft-card flex items-center justify-between gap-3 p-4">
              <div>
                <div className="font-semibold">{r.resourceName} <span className="text-xs">{r.resourceCode}</span></div>
                <div className="text-sm">{r.reason}</div>
              </div>
              <Link to={`/book/${r.resourceId}`} className="btn-primary shrink-0">Book</Link>
            </motion.div>
          ))}
          {res.recommendations.length === 0 && <p>No matching resource exists in SQL. Try another type, time or the campus map.</p>}
        </div>
      )}
    </div>
  )
}
