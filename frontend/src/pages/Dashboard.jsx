import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import toast from 'react-hot-toast'
import { occupancyColor } from '../utils/status'
import { useAuth } from '../context/AuthContext'
import client from '../api/client'
import StatusBadge from '../components/StatusBadge'
import CampusMap from '../components/campus/CampusMap'
import { PageHeader, StatCard } from '../components/ui'
import { errorMessage } from '../utils/status'

export default function Dashboard() {
  const { user } = useAuth()
  const [data, setData] = useState(null)
  const [heat, setHeat] = useState([])
  const [buildings, setBuildings] = useState([])
  const [mine, setMine] = useState([])
  const [now, setNow] = useState([])
  const [quick, setQuick] = useState({
    typeCode: 'CLASSROOM',
    date: new Date().toISOString().slice(0, 10),
    startTime: '10:00',
    endTime: '12:00',
    minCapacity: 30,
  })
  const [results, setResults] = useState([])

  const load = () => {
    client.get('/dashboard').then((r) => setData(r.data))
    client.get('/analytics/heatmap').then((r) => setHeat(r.data)).catch(() => {})
    client.get('/campus').then((r) => setBuildings(r.data))
    client.get('/bookings/my').then((r) => setMine(r.data || []))
    client.get('/available-now').then((r) => setNow((r.data || []).slice(0, 8)))
  }

  useEffect(() => { load() }, [])

  const find = async (e) => {
    e.preventDefault()
    const { data: rows } = await client.get('/resources', { params: quick })
    setResults(rows.slice(0, 8))
  }

  const cancel = async (b) => {
    if (!window.confirm(`Cancel “${b.title}”?`)) return
    try {
      await client.post(`/bookings/${b.id}/cancel`)
      toast.success('Cancelled')
      load()
    } catch (err) {
      toast.error(errorMessage(err))
    }
  }

  if (!data) return <div className="soft-card h-72 animate-pulse" />
  const live = data.live || {}
  const first = user.fullName.split(' ')[0]
  const activeMine = mine.filter((b) => !['CANCELLED', 'REJECTED', 'COMPLETED', 'NO_SHOW'].includes(b.status)).slice(0, 4)

  return (
    <div className="w-full space-y-6">
      <PageHeader
        kicker="Command deck"
        title={`Welcome back, ${first}`}
        subtitle="Full-campus occupancy, your bookings, and a one-tap room search — all from SQL."
        actions={<div className="flex gap-2"><Link to="/bookings" className="btn-ghost">My bookings</Link><Link to="/book" className="btn-primary">Book a room</Link></div>}
      />

      <div className="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-6">
        <StatCard label="Free now" value={live.available ?? '—'} accent="#16a34a" />
        <StatCard label="In use" value={live.occupied ?? '—'} accent="#c14a32" />
        <StatCard label="Occupancy" value={`${live.occupancyPercent || 0}%`} />
        <StatCard label="Your pending" value={data.pending} />
        <StatCard label="Confirmed" value={data.confirmed} />
        <StatCard label={user.role === 'STUDENT' ? 'Completed' : 'Approvals'} value={user.role === 'STUDENT' ? data.completed : (data.pendingApprovals ?? 0)} />
      </div>

      {data.upcoming && (
        <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }}>
          <Link to={`/bookings/${data.upcoming.id}`} className="block overflow-hidden rounded-[1.4rem] bg-grove-800 p-6 text-cream-50 shadow-lg">
            <div className="text-xs uppercase tracking-[0.22em] text-gold-400">Next booking</div>
            <div className="mt-1 font-display text-3xl md:text-5xl">{data.upcoming.title}</div>
            <div className="mt-2 text-cream-200">{data.upcoming.date} · {data.upcoming.startTime}–{data.upcoming.endTime} · {data.upcoming.resources?.[0]?.name}</div>
          </Link>
        </motion.div>
      )}

      <div className="grid gap-6 xl:grid-cols-[1.35fr_.65fr]">
        <motion.section initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="min-w-0">
          <CampusMap buildings={buildings} heatmap={heat} />
        </motion.section>
        <section className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="font-display text-2xl">Your bookings</h2>
            <Link to="/bookings" className="text-sm text-grove-600">View all</Link>
          </div>
          {activeMine.map((b) => (
            <div key={b.id} className="soft-card p-4">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <Link to={`/bookings/${b.id}`} className="font-semibold hover:underline">{b.title}</Link>
                  <div className="text-xs text-grove-500">{b.date} · {b.startTime}–{b.endTime}</div>
                </div>
                <StatusBadge status={b.status} />
              </div>
              <div className="mt-3 flex gap-2">
                <Link to={`/bookings/${b.id}`} className="btn-ghost text-xs">Open</Link>
                {b.status !== 'CHECKED_IN' && b.status !== 'COMPLETED' && (
                  <button className="text-xs text-brick-500" onClick={() => cancel(b)}>Cancel</button>
                )}
              </div>
            </div>
          ))}
          {activeMine.length === 0 && <p className="text-sm text-grove-500">No active bookings. <Link className="underline" to="/book">Book a room</Link></p>}
          <div>
            <h2 className="mb-2 font-display text-2xl">Free right now</h2>
            <div className="space-y-2">
              {now.map((r) => (
                <Link key={r.id} to={`/book/${r.id}`} className="block rounded-xl border border-cream-200 px-3 py-2 text-sm hover:bg-white dark:border-white/10">
                  {r.name} <span className="text-grove-500">· {r.buildingName}</span>
                </Link>
              ))}
            </div>
          </div>
        </section>
      </div>

      {heat.length > 0 && (
        <section>
          <h2 className="mb-3 font-display text-2xl">Block load</h2>
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            {heat.map((h, i) => (
              <motion.div key={h.buildingId} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.04 }}>
                <Link to={`/buildings/${h.buildingId}`} className="soft-card block p-4">
                  <div className="flex items-center justify-between">
                    <span className="font-semibold">{h.name}</span>
                    <span className="h-2.5 w-2.5 rounded-full" style={{ background: occupancyColor(h.level) }} />
                  </div>
                  <div className="font-display text-3xl">{h.percent}%</div>
                  <div className="text-xs text-grove-500">{h.available}/{h.total} free</div>
                </Link>
              </motion.div>
            ))}
          </div>
        </section>
      )}

      {user.role === 'ADMIN' && (
        <div className="grid gap-4 lg:grid-cols-2">
          <div className="soft-card p-4">
            <div className="mb-2 font-display text-xl">Booking trend</div>
            <div className="h-52">
              <ResponsiveContainer>
                <AreaChart data={data.bookingTrends || []}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e8dfcc" />
                  <XAxis dataKey="date" hide />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Area type="monotone" dataKey="count" stroke="#146c4a" fill="#146c4a33" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>
          <div className="soft-card p-4">
            <div className="mb-2 font-display text-xl">Peak hours</div>
            <div className="h-52">
              <ResponsiveContainer>
                <AreaChart data={data.peakHours || []}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e8dfcc" />
                  <XAxis dataKey="hour" />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Area type="monotone" dataKey="count" stroke="#c14a32" fill="#c14a3222" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      )}

      <form onSubmit={find} className="soft-card grid gap-3 p-4 md:grid-cols-6">
        <select className="field" value={quick.typeCode} onChange={(e) => setQuick({ ...quick, typeCode: e.target.value })}>
          <option>CLASSROOM</option><option>LABORATORY</option><option>SEMINAR_HALL</option><option>LIBRARY</option>
        </select>
        <input type="date" className="field" value={quick.date} onChange={(e) => setQuick({ ...quick, date: e.target.value })} />
        <input type="time" className="field" value={quick.startTime} onChange={(e) => setQuick({ ...quick, startTime: e.target.value })} />
        <input type="time" className="field" value={quick.endTime} onChange={(e) => setQuick({ ...quick, endTime: e.target.value })} />
        <input type="number" className="field" value={quick.minCapacity} onChange={(e) => setQuick({ ...quick, minCapacity: e.target.value })} />
        <button className="btn-primary">Find free rooms</button>
      </form>
      <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
        {results.map((r) => (
          <Link key={r.id} to={`/book/${r.id}`} className="soft-card p-4 transition hover:-translate-y-0.5 hover:shadow-md">
            <div className="font-semibold">{r.name}</div>
            <div className="text-sm text-grove-500">{r.buildingName} · cap {r.capacity}</div>
          </Link>
        ))}
      </div>
    </div>
  )
}
