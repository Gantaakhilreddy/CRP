import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client from '../api/client'
import StatusBadge from '../components/StatusBadge'

export default function AvailableNow() {
  const [rows, setRows] = useState([])
  const [type, setType] = useState('')
  useEffect(() => {
    client.get('/available-now', { params: { typeCode: type || undefined } }).then((r) => setRows(r.data))
  }, [type])
  const grouped = rows.reduce((acc, r) => {
    acc[r.buildingName] = acc[r.buildingName] || []
    acc[r.buildingName].push(r)
    return acc
  }, {})
  return (
    <div>
      <h1 className="font-display text-4xl">What’s available now?</h1>
      <select className="my-4 rounded-xl border px-3 py-2 dark:bg-grove-800" value={type} onChange={(e) => setType(e.target.value)}>
        <option value="">All types</option>
        <option>CLASSROOM</option><option>LABORATORY</option><option>SEMINAR_HALL</option><option>LIBRARY</option>
      </select>
      {Object.entries(grouped).map(([b, items]) => (
        <section key={b} className="mb-6">
          <h2 className="font-display text-2xl">{b}</h2>
          <div className="mt-2 grid gap-2 md:grid-cols-2">
            {items.map((r) => (
              <Link key={r.id} to={`/resources/${r.id}`} className="soft-card flex items-center justify-between p-3">
                <span>{r.name}</span>
                <StatusBadge status={r.status} />
              </Link>
            ))}
          </div>
        </section>
      ))}
    </div>
  )
}
