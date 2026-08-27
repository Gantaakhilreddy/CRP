import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import client from '../../api/client'
import { downloadFile } from '../../components/AuthImage'
import { PageHeader, Skeleton } from '../../components/ui'
import { errorMessage } from '../../utils/status'
import AnalyticsDashboard from './AnalyticsDashboard'
import ResourceManager from './ResourceManager'

export default function AdminHome() {
  const [tab, setTab] = useState('overview')
  const [users, setUsers] = useState([])
  const [issues, setIssues] = useState([])
  const [maint, setMaint] = useState([])
  const [floors, setFloors] = useState([])
  const [insight, setInsight] = useState('')
  const [q, setQ] = useState('Which building is most utilized?')
  const [from, setFrom] = useState(new Date(Date.now() - 14 * 86400000).toISOString().slice(0, 10))
  const [to, setTo] = useState(new Date().toISOString().slice(0, 10))
  const [loadingTab, setLoadingTab] = useState(false)

  useEffect(() => {
    if (tab === 'floors') {
      setLoadingTab(true)
      client.get('/floors').then((r) => setFloors(r.data || [])).catch(async () => {
        const campus = await client.get('/campus')
        const all = []
        for (const b of campus.data || []) {
          const d = await client.get(`/buildings/${b.id}`)
          ;(d.data.floors || []).forEach((f) => all.push(f))
        }
        setFloors(all)
      }).finally(() => setLoadingTab(false))
    }
    if (tab === 'users') {
      setLoadingTab(true)
      client.get('/admin/users').then((r) => setUsers(r.data)).finally(() => setLoadingTab(false))
    }
    if (tab === 'issues') {
      setLoadingTab(true)
      client.get('/issues').then((r) => setIssues(r.data)).finally(() => setLoadingTab(false))
    }
    if (tab === 'maintenance') {
      setLoadingTab(true)
      client.get('/admin/maintenance').then((r) => setMaint(r.data)).finally(() => setLoadingTab(false))
    }
  }, [tab])

  const ask = async (e) => {
    e.preventDefault()
    const { data } = await client.post('/ai/insights', { question: q })
    setInsight(data.reply)
  }

  return (
    <div className="w-full">
      {tab !== 'overview' && tab !== 'resources' && (
        <PageHeader kicker="Administration" title="Campus operating console" subtitle="Analytics, floors, people and exports. Occupancy and charts come from SQL." />
      )}
      <div className="my-4 flex flex-wrap gap-2">
        {['overview', 'resources', 'floors', 'users', 'issues', 'maintenance', 'reports', 'insights'].map((t) => (
          <button key={t} onClick={() => setTab(t)} className={`rounded-full px-4 py-1.5 text-sm capitalize ${tab === t ? 'bg-grove-700 text-white' : 'bg-white dark:bg-grove-800'}`}>{t}</button>
        ))}
      </div>
      {tab === 'overview' && <AnalyticsDashboard />}
      {tab === 'resources' && <ResourceManager />}
      {tab === 'floors' && (
        loadingTab ? <Skeleton className="h-40" /> : (
          <div className="grid gap-2 md:grid-cols-2">
            {floors.map((f) => (
              <Link key={f.id} to={`/admin/floors/${f.id}/editor`} className="soft-card p-4 hover:shadow-md">{f.buildingName} · {f.name} · {f.availableNow}/{f.totalResources} free</Link>
            ))}
          </div>
        )
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
        <div className="soft-card space-y-4 p-5">
          <p className="text-sm text-grove-600">Professional PDF includes KPIs, utilization, forecasts (labeled), and the booking register for the selected range.</p>
          <div className="flex flex-wrap gap-3">
            <label className="text-xs">From<input type="date" className="field mt-1" value={from} onChange={(e) => setFrom(e.target.value)} /></label>
            <label className="text-xs">To<input type="date" className="field mt-1" value={to} onChange={(e) => setTo(e.target.value)} /></label>
          </div>
          <div className="flex flex-wrap gap-3">
            <button className="btn-primary" onClick={() => downloadFile(`/admin/reports/csv?from=${from}&to=${to}`, 'bookings.csv')}>CSV</button>
            <button className="btn-primary" onClick={() => downloadFile(`/admin/reports/excel?from=${from}&to=${to}`, 'bookings.xlsx')}>Excel</button>
            <button className="btn-primary" onClick={() => downloadFile(`/admin/reports/pdf?from=${from}&to=${to}`, `campusos-analytics-${from}-to-${to}.pdf`)}>PDF report</button>
          </div>
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
