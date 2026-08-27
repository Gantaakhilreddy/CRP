import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import client from '../api/client'
import StatusBadge from '../components/StatusBadge'

export default function SearchPage() {
  const [params, setParams] = useSearchParams()
  const [q, setQ] = useState(params.get('q') || '')
  const [rows, setRows] = useState([])

  const run = async (query) => {
    const { data } = await client.get('/resources', { params: { q: query } })
    setRows(data)
  }

  useEffect(() => { run(q) }, [])

  return (
    <div>
      <h1 className="font-display text-4xl">Search</h1>
      <form className="my-4 flex gap-2" onSubmit={(e) => { e.preventDefault(); setParams({ q }); run(q) }}>
        <input className="flex-1 rounded-full border px-4 py-2 dark:bg-grove-800" value={q} onChange={(e) => setQ(e.target.value)} placeholder="Room 204, Physics Lab, Central Block..." />
        <button className="rounded-full bg-grove-700 px-5 py-2 text-white">Search</button>
      </form>
      <div className="space-y-2">
        {rows.map((r) => (
          <Link key={r.id} to={`/resources/${r.id}`} className="soft-card flex items-center justify-between p-4">
            <div>
              <div className="font-semibold">{r.name}</div>
              <div className="text-xs">{r.buildingName} · {r.floorName} · {r.typeName}</div>
            </div>
            <StatusBadge status={r.status} />
          </Link>
        ))}
      </div>
    </div>
  )
}
