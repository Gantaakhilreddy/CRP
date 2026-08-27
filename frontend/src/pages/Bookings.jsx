import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import client from '../api/client'
import StatusBadge from '../components/StatusBadge'
import { errorMessage } from '../utils/status'
import { PageHeader } from '../components/ui'

const TABS = [
  { id: 'upcoming', label: 'Upcoming' },
  { id: 'pending', label: 'Waiting approval' },
  { id: 'past', label: 'Past' },
  { id: 'all', label: 'All' },
]

function bucket(b) {
  const status = b.status
  if (['PENDING_PROFESSOR', 'PENDING_ADMIN'].includes(status)) return 'pending'
  if (['COMPLETED', 'CANCELLED', 'REJECTED', 'NO_SHOW'].includes(status)) return 'past'
  return 'upcoming'
}

function canCancel(b) {
  return !['COMPLETED', 'CANCELLED', 'REJECTED', 'NO_SHOW'].includes(b.status)
}

export default function Bookings() {
  const [rows, setRows] = useState([])
  const [tab, setTab] = useState('upcoming')
  const [q, setQ] = useState('')

  const load = () => client.get('/bookings/my').then((r) => setRows(r.data || []))
  useEffect(() => { load() }, [])

  const visible = useMemo(() => {
    return rows.filter((b) => {
      if (tab !== 'all' && bucket(b) !== tab) return false
      if (!q) return true
      const hay = `${b.title} ${b.purpose || ''} ${(b.resources || []).map((r) => r.name).join(' ')}`.toLowerCase()
      return hay.includes(q.toLowerCase())
    })
  }, [rows, tab, q])

  const act = async (id, path, ok) => {
    try {
      await client.post(`/bookings/${id}/${path}`)
      toast.success(ok)
      load()
    } catch (err) {
      toast.error(errorMessage(err))
    }
  }

  const cancel = (b) => {
    if (!window.confirm(`Cancel “${b.title}” on ${b.date}? The room will be freed.`)) return
    act(b.id, 'cancel', 'Booking cancelled')
  }

  return (
    <div className="w-full">
      <PageHeader
        title="My bookings"
        subtitle="See what’s coming, cancel what you no longer need, and check in on the day."
        actions={<Link to="/book" className="btn-primary">New booking</Link>}
      />
      <div className="mb-4 flex flex-wrap items-center gap-2">
        {TABS.map((t) => (
          <button key={t.id} onClick={() => setTab(t.id)} className={`rounded-full px-4 py-1.5 text-sm ${tab === t.id ? 'bg-grove-700 text-white' : 'bg-white dark:bg-grove-800'}`}>
            {t.label}
          </button>
        ))}
        <input className="field ml-auto max-w-xs rounded-full" placeholder="Search your bookings…" value={q} onChange={(e) => setQ(e.target.value)} />
      </div>
      <div className="grid gap-3 lg:grid-cols-2">
        {visible.map((b) => (
          <article key={b.id} className="soft-card p-5">
            <div className="flex items-start justify-between gap-3">
              <div>
                <Link to={`/bookings/${b.id}`} className="font-display text-2xl hover:underline">{b.title}</Link>
                <div className="text-sm text-grove-600">{b.date} · {b.startTime}–{b.endTime}</div>
                <div className="text-xs text-grove-500">{b.resources?.map((r) => `${r.name} · ${r.buildingName}`).join(', ')}</div>
              </div>
              <StatusBadge status={b.status} />
            </div>
            <div className="mt-4 flex flex-wrap gap-2">
              <Link to={`/bookings/${b.id}`} className="btn-ghost">Details</Link>
              {b.status === 'CONFIRMED' && <button className="btn-primary" onClick={() => act(b.id, 'check-in', 'Checked in')}>Check in</button>}
              {b.status === 'CHECKED_IN' && <button className="btn-primary" onClick={() => act(b.id, 'check-out', 'Checked out')}>Check out</button>}
              {canCancel(b) && <button className="btn-ghost text-brick-500" onClick={() => cancel(b)}>Cancel</button>}
            </div>
          </article>
        ))}
      </div>
      {visible.length === 0 && (
        <div className="soft-card p-10 text-center">
          <p className="font-display text-2xl">Nothing here yet</p>
          <Link to="/book" className="btn-primary mt-4 inline-flex">Book a room</Link>
        </div>
      )}
    </div>
  )
}
