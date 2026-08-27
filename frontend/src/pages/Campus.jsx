import { useEffect, useState } from 'react'
import client from '../api/client'
import CampusMap from '../components/campus/CampusMap'
import { occupancyColor } from '../utils/status'

export default function Campus() {
  const [buildings, setBuildings] = useState([])
  const [heat, setHeat] = useState([])
  const [live, setLive] = useState(null)

  useEffect(() => {
    client.get('/campus').then((r) => setBuildings(r.data))
    client.get('/analytics/heatmap').then((r) => setHeat(r.data))
    client.get('/analytics/live').then((r) => setLive(r.data))
  }, [])

  return (
    <div className="w-full space-y-6">
      {live && (
        <div className="grid grid-cols-2 gap-3 md:grid-cols-5">
          {[
            ['Available', live.available, '#16a34a'],
            ['Occupied', live.occupied, '#dc2626'],
            ['Maintenance', live.maintenance, '#ca8a04'],
            ['Blocked', live.blocked, '#334155'],
            ['Occupancy', `${live.occupancyPercent}%`, '#d4a017'],
          ].map(([l, v, c]) => (
            <div key={l} className="soft-card p-4">
              <div className="text-xs uppercase tracking-wide text-grove-500">{l}</div>
              <div className="font-display text-3xl" style={{ color: c }}>{v}</div>
            </div>
          ))}
        </div>
      )}
      <CampusMap buildings={buildings} heatmap={heat} />
      <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-4">
        {buildings.map((b) => (
          <a key={b.id} href={`/buildings/${b.id}`} className="soft-card p-4 hover:shadow-md">
            <div className="flex items-center justify-between">
              <div className="font-display text-xl">{b.name}</div>
              <span className="h-3 w-3 rounded-full" style={{ background: occupancyColor(b.liveStatus) }} />
            </div>
            <div className="text-xs uppercase tracking-widest text-grove-500">{b.virtueName}</div>
            <p className="mt-2 line-clamp-2 text-sm text-grove-600 dark:text-grove-200">{b.description}</p>
            <div className="mt-3 text-sm">{b.availableNow}/{b.resources} available · {b.floors} floors</div>
          </a>
        ))}
      </div>
    </div>
  )
}
