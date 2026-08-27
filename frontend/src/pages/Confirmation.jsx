import { Link, useParams } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import client from '../api/client'
import { downloadFile } from '../components/AuthImage'

export default function Confirmation() {
  const { id } = useParams()
  const [b, setB] = useState(null)
  useEffect(() => { client.get(`/bookings/${id}`).then((r) => setB(r.data)) }, [id])
  if (!b) return null
  return (
    <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} className="mx-auto max-w-lg text-center">
      <div className="mx-auto grid h-16 w-16 place-items-center rounded-full bg-grove-700 text-2xl text-white">✓</div>
      <h1 className="mt-4 font-display text-4xl">Booking requested</h1>
      <p className="mt-2 text-grove-600">{b.resources?.[0]?.name} · {b.resources?.[0]?.buildingName}</p>
      <p className="mt-1">{b.date} · {b.startTime} – {b.endTime}</p>
      <p className="mt-4 rounded-full bg-gold-400/20 px-4 py-2 text-sm">Status: {String(b.status).replaceAll('_', ' ')}</p>
      <div className="mt-6 flex justify-center gap-3">
        <Link to={`/bookings/${b.id}`} className="btn-primary">View booking</Link>
        <button onClick={() => downloadFile(`/bookings/${b.id}/calendar`, `booking-${b.id}.ics`)} className="btn-ghost">Add to calendar</button>
      </div>
    </motion.div>
  )
}
