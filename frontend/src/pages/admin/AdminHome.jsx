import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import toast from 'react-hot-toast'
import client from '../../api/client'
import { downloadFile } from '../../components/AuthImage'
import { occupancyColor } from '../../utils/status'
import { PageHeader, StatCard } from '../../components/ui'

export default function AdminHome() {
  const [dash, setDash] = useState(null)
  const [tab, setTab] = useState('overview')
  const [users, setUsers] = useState([])
  const [issues, setIssues] = useState([])
  const [maint, setMaint] = useState([])
  const [floors, setFloors] = useState([])
  const [heat, setHeat] = useState([])
  const [insight, setInsight] = useState('')
  const [q, setQ] = useState('Which building is most utilized?')

  useEffect(() => {
    client.get('/dashboard').then((r) => setDash(r.data))
    client.get('/analytics/heatmap').then((r) => setHeat(r.data)).catch(() => {})
    client.get('/campus').then(async (r) => {
      const all = []
      for (const b of r.data) {
        const d = await client.get(`/buildings/${b.id}`)
        d.data.floors.forEach((f) => all.push(f))
      }
      setFloors(all)
    })
  }, [])

  useEffect(() => {
    if (tab === 'users') client.get('/admin/users').then((r) => setUsers(r.data))
    if (tab === 'issues') client.get('/issues').then((r) => setIssues(r.data))
    if (tab === 'maintenance') client.get('/admin/maintenance').then((r) => setMaint(r.data))
  }, [tab])

  const ask = async (e) => {
    e.preventDefault()
    const { data } = await client.post('/ai/insights', { question: q })
    setInsight(data.reply)
  }

  const from = new Date(Date.now() - 14 * 86400000).toISOString().slice(0, 10)
  const to = new Date().toISOString().slice(0, 10)

  if (!dash) return <div className="soft-card h-64 animate-pulse" />

  return (
    <div className="w-full">
      <PageHeader kicker="Administration" title="Campus operating console" subtitle="Every tile is a SQL aggregate — occupancy, peak hours and underused rooms included." />
      <div className="my-4 flex flex-wrap gap-2">
        {['overview', 'floors', 'users', 'issues', 'maintenance', 'reports', 'insights'].map((t) => (
          <button key={t} onClick={() => setTab(t)} className={`rounded-full px-4 py-1.5 text-sm capitalize ${tab === t ? 'bg-grove-700 text-white' : 'bg-white dark:bg-grove-800'}`}>{t}</button>
        ))}
      </div>
      {tab === 'overview' && (
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
            <StatCard label="Buildings" value={dash.totalBuildings} />
            <StatCard label="Resources" value={dash.totalResources} />
            <StatCard label="Available now" value={dash.availableNow} accent="#16a34a" />
            <StatCard label="Occupancy" value={`${dash.occupancyPercent}%`} accent="#c14a32" />
            <StatCard label="Bookings today" value={dash.bookingsToday} />
            <StatCard label="Pending" value={dash.pendingApprovals} />
            <StatCard label="Open issues" value={dash.openIssues} />
            <StatCard label="Floors" value={dash.totalFloors} />
          </div>
          <div className="grid gap-3 md:grid-cols-4">
            {heat.map((h) => (
              <div key={h.buildingId} className="soft-card p-4">
                <div className="flex items-center justify-between">
                  <span className="font-semibold">{h.name}</span>
                  <span className="h-2.5 w-2.5 rounded-full" style={{ background: occupancyColor(h.level) }} />
                </div>
                <div className="font-display text-3xl">{h.percent}%</div>
              </div>
            ))}
          </div>
          <div className="grid gap-4 lg:grid-cols-2">
            <div className="soft-card h-64 p-4">
              <div className="mb-2 font-display text-xl">Peak hours</div>
              <ResponsiveContainer>
                <AreaChart data={dash.peakHours || []}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e8dfcc" />
                  <XAxis dataKey="hour" />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Area type="monotone" dataKey="count" stroke="#c14a32" fill="#c14a3222" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
            <div className="soft-card h-64 p-4">
              <div className="mb-2 font-display text-xl">14-day bookings</div>
              <ResponsiveContainer>
                <AreaChart data={dash.bookingTrends || []}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e8dfcc" />
                  <XAxis dataKey="date" hide />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Area type="monotone" dataKey="count" stroke="#146c4a" fill="#146c4a33" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>
          {dash.underutilized?.length > 0 && (
            <div className="soft-card p-5">
              <div className="font-display text-xl">Underused rooms</div>
              <div className="mt-3 space-y-2 text-sm">
                {dash.underutilized.map((u) => (
                  <div key={u.resourceId}>{u.name} · {u.utilization}% · {u.recommendation}</div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
      {tab === 'floors' && (
        <div className="grid gap-2 md:grid-cols-2">
          {floors.map((f) => (
            <Link key={f.id} to={`/admin/floors/${f.id}/editor`} className="soft-card p-4 hover:shadow-md">{f.buildingName} · {f.name}</Link>
          ))}
        </div>
      )}
      {tab === 'users' && (
        <div className="overflow-x-auto soft-card">
          <table className="w-full text-sm">
            <thead><tr><th className="p-2 text-left">Name</th><th>Email</th><th>Role</th></tr></thead>
            <tbody>{users.map((u) => <tr key={u.id} className="border-t"><td className="p-2">{u.fullName}</td><td>{u.email}</td><td>{u.role}</td></tr>)}</tbody>
          </table>
        </div>
      )}
      {tab === 'issues' && issues.map((i) => (
        <div key={i.id} className="soft-card mb-2 flex items-center justify-between p-4">
          <div>{i.category} · {i.status}<div className="text-sm">{i.description}</div></div>
          <button className="text-sm" onClick={async () => { await client.put(`/issues/${i.id}`, { status: 'RESOLVED', resolution: 'Fixed' }); toast.success('Resolved') }}>Resolve</button>
        </div>
      ))}
      {tab === 'maintenance' && maint.map((m) => (
        <div key={m.id} className="soft-card mb-2 p-4">{m.reason} · {m.startDate}–{m.endDate}</div>
      ))}
      {tab === 'reports' && (
        <div className="flex flex-wrap gap-3">
          <button className="btn-primary" onClick={() => downloadFile(`/admin/reports/csv?from=${from}&to=${to}`, 'bookings.csv')}>CSV</button>
          <button className="btn-primary" onClick={() => downloadFile(`/admin/reports/excel?from=${from}&to=${to}`, 'bookings.xlsx')}>Excel</button>
          <button className="btn-primary" onClick={() => downloadFile(`/admin/reports/pdf?from=${from}&to=${to}`, 'bookings.pdf')}>PDF</button>
        </div>
      )}
      {tab === 'insights' && (
        <form onSubmit={ask} className="soft-card space-y-3 p-5">
          <input className="field" value={q} onChange={(e) => setQ(e.target.value)} />
          <button className="btn-primary">Ask CampusOS</button>
          {insight && <p className="text-sm leading-relaxed">{insight}</p>}
        </form>
      )}
    </div>
  )
}
