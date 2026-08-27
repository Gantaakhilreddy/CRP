import { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { motion } from 'framer-motion'
import toast from 'react-hot-toast'
import client from '../api/client'
import { errorMessage } from '../utils/status'
import StatusBadge from '../components/StatusBadge'
import { PageHeader } from '../components/ui'

export default function Book() {
  const { resourceId } = useParams()
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const [buildings, setBuildings] = useState([])
  const [rooms, setRooms] = useState([])
  const [resource, setResource] = useState(null)
  const [timeline, setTimeline] = useState([])
  const [availability, setAvailability] = useState(null)
  const [saving, setSaving] = useState(false)
  const [filters, setFilters] = useState({ q: '', typeCode: '', buildingId: '', minCapacity: '' })
  const [form, setForm] = useState({
    date: params.get('date') || new Date(Date.now() + 86400000).toISOString().slice(0, 10),
    startTime: (params.get('start') || '10:00').slice(0, 5),
    endTime: (params.get('end') || '11:00').slice(0, 5),
    purpose: '',
    attendees: 20,
  })

  const loadRooms = () => {
    client.get('/resources', {
      params: {
        q: filters.q || undefined,
        typeCode: filters.typeCode || undefined,
        buildingId: filters.buildingId || undefined,
        minCapacity: filters.minCapacity || undefined,
      },
    }).then((r) => setRooms(r.data))
  }

  useEffect(() => {
    client.get('/campus').then((r) => setBuildings(r.data.filter((b) => b.bookable)))
  }, [])

  useEffect(() => { loadRooms() }, [filters.q, filters.typeCode, filters.buildingId, filters.minCapacity])

  useEffect(() => {
    const id = resourceId || resource?.id
    if (!id) return
    client.get(`/resources/${id}`, { params: { date: form.date } }).then((r) => {
      setResource(r.data.resource)
      setTimeline(r.data.timeline || [])
    })
  }, [resourceId, resource?.id, form.date])

  useEffect(() => {
    if (!resource) return
    client.get(`/resources/${resource.id}/availability`, {
      params: { date: form.date, startTime: form.startTime, endTime: form.endTime },
    }).then((r) => setAvailability(r.data)).catch(() => setAvailability(null))
  }, [resource, form.date, form.startTime, form.endTime])

  const pickSlot = (hour, available) => {
    if (!available) return
    const start = (hour || '').slice(0, 5)
    const [h, m] = start.split(':').map(Number)
    setForm((f) => ({ ...f, startTime: start, endTime: `${String(h + 1).padStart(2, '0')}:${String(m).padStart(2, '0')}` }))
  }

  const submit = async () => {
    if (!resource) return toast.error('Pick a room first')
    if (availability && !availability.available) return toast.error(availability.reason || 'That time is taken')
    setSaving(true)
    try {
      const { data } = await client.post('/bookings', {
        resourceIds: [resource.id],
        date: form.date,
        startTime: form.startTime.slice(0, 5),
        endTime: form.endTime.slice(0, 5),
        title: form.purpose || resource.name,
        purpose: form.purpose,
        attendees: Number(form.attendees),
      })
      toast.success('Booking sent')
      navigate(`/bookings/${data.id}/confirmation`)
    } catch (err) {
      toast.error(errorMessage(err, 'Could not book'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="w-full">
      <PageHeader title="Book a room" subtitle="Choose a room, tap a green hour, add why you need it, and send. Availability is live from MySQL." />
      <div className="grid gap-6 xl:grid-cols-[1.05fr_.95fr]">
        <section className="soft-card p-4">
          <div className="mb-3 grid gap-2 md:grid-cols-4">
            <input className="field md:col-span-2" placeholder="Search rooms…" value={filters.q} onChange={(e) => setFilters({ ...filters, q: e.target.value })} />
            <select className="field" value={filters.buildingId} onChange={(e) => setFilters({ ...filters, buildingId: e.target.value })}>
              <option value="">All blocks</option>
              {buildings.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
            </select>
            <select className="field" value={filters.typeCode} onChange={(e) => setFilters({ ...filters, typeCode: e.target.value })}>
              <option value="">All types</option>
              <option>CLASSROOM</option><option>LABORATORY</option><option>SEMINAR_HALL</option><option>LIBRARY</option>
            </select>
          </div>
          <div className="max-h-[62vh] space-y-2 overflow-y-auto pr-1">
            {rooms.map((r) => (
              <button
                key={r.id}
                onClick={() => { setResource(r); navigate(`/book/${r.id}`, { replace: true }) }}
                className={`w-full rounded-2xl border p-3 text-left transition ${resource?.id === r.id ? 'border-grove-700 bg-grove-50 dark:bg-grove-800' : 'border-cream-200 hover:border-grove-400 dark:border-white/10'}`}
              >
                <div className="flex items-center justify-between gap-2">
                  <div>
                    <div className="font-semibold">{r.name}</div>
                    <div className="text-xs text-grove-500">{r.buildingName} · {r.floorName} · {r.capacity} seats</div>
                  </div>
                  <StatusBadge status={r.status} />
                </div>
              </button>
            ))}
          </div>
        </section>

        <section className="soft-card p-5">
          {!resource && <p className="text-grove-500">Select a room on the left to see free hours.</p>}
          {resource && (
            <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="space-y-4">
              <div>
                <div className="text-xs uppercase tracking-[0.2em] text-grove-500">{resource.buildingName}</div>
                <h2 className="font-display text-3xl">{resource.name}</h2>
                <p className="text-sm">{resource.floorName} · Capacity {resource.capacity}</p>
              </div>
              <label className="text-sm font-medium">Date
                <input type="date" className="field mt-1" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} />
              </label>
              <div>
                <div className="mb-2 text-sm font-medium">Tap a free hour</div>
                <div className="grid grid-cols-3 gap-2 sm:grid-cols-4">
                  {timeline.map((slot) => (
                    <button
                      key={slot.hour}
                      type="button"
                      disabled={!slot.available}
                      onClick={() => pickSlot(slot.hour, slot.available)}
                      className={`rounded-xl px-2 py-2 text-sm ${slot.available ? 'bg-green-100 text-green-900' : 'cursor-not-allowed bg-red-50 text-red-400 line-through'} ${form.startTime === (slot.hour || '').slice(0, 5) ? 'ring-2 ring-grove-700' : ''}`}
                    >
                      {(slot.hour || '').slice(0, 5)}
                    </button>
                  ))}
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <label className="text-sm">From<input type="time" className="field mt-1" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} /></label>
                <label className="text-sm">To<input type="time" className="field mt-1" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} /></label>
              </div>
              {availability && (
                <div className={`rounded-xl p-3 text-sm ${availability.available ? 'bg-green-100 text-green-900' : 'bg-red-100 text-red-800'}`}>
                  {availability.available ? `Free on ${form.date} ${form.startTime}–${form.endTime}` : availability.reason}
                </div>
              )}
              <input className="field" placeholder="Why do you need this room?" value={form.purpose} onChange={(e) => setForm({ ...form, purpose: e.target.value })} />
              <label className="text-sm">How many people?
                <input type="number" className="field mt-1" value={form.attendees} onChange={(e) => setForm({ ...form, attendees: e.target.value })} />
              </label>
              <button className="btn-primary w-full py-3" disabled={saving || (availability && !availability.available)} onClick={submit}>
                {saving ? 'Booking…' : 'Send booking'}
              </button>
            </motion.div>
          )}
        </section>
      </div>
    </div>
  )
}
