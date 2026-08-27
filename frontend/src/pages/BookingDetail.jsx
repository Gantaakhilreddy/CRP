import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import client from '../api/client'
import { errorMessage } from '../utils/status'
import { downloadFile } from '../components/AuthImage'
import { PageHeader } from '../components/ui'

const flow = [
  { key: 'PENDING_PROFESSOR', label: 'Requested' },
  { key: 'PENDING_ADMIN', label: 'Professor approved' },
  { key: 'CONFIRMED', label: 'Confirmed' },
  { key: 'CHECKED_IN', label: 'Checked in' },
  { key: 'COMPLETED', label: 'Completed' },
]

export default function BookingDetail() {
  const { id } = useParams()
  const [b, setB] = useState(null)

  const load = () => client.get(`/bookings/${id}`).then((r) => setB(r.data))
  useEffect(() => { load() }, [id])

  if (!b) return <div className="soft-card h-48 animate-pulse" />

  const act = async (path) => {
    try {
      await client.post(`/bookings/${id}/${path}`)
      toast.success('Updated')
      load()
    } catch (err) {
      toast.error(errorMessage(err))
    }
  }

  const idx = flow.findIndex((f) => f.key === b.status)

  return (
    <div className="space-y-6">
      <PageHeader
        kicker={b.bookingKind}
        title={b.title}
        subtitle={`${b.date} · ${b.startTime}–${b.endTime} · ${b.resources?.map((r) => `${r.name} · ${r.buildingName}`).join(' · ')}`}
      />
      <div className="soft-card p-6">
        {flow.map((item, i) => (
          <div key={item.key} className="flex gap-3">
            <div className="flex flex-col items-center">
              <span className={`h-3 w-3 rounded-full ${i <= idx || (b.status === 'CONFIRMED' && i <= 2) ? 'bg-grove-600' : 'bg-grove-200'}`} />
              {i < flow.length - 1 && <span className="h-8 w-px bg-grove-200" />}
            </div>
            <div className="-mt-1 pb-4">
              <div className="font-semibold">{item.label}</div>
            </div>
          </div>
        ))}
        {b.status === 'REJECTED' && <div className="text-brick-500">Rejected: {b.rejectionReason}</div>}
      </div>
      <div className="flex flex-wrap gap-2">
        <button onClick={() => act('check-in')} className="btn-primary">Check in</button>
        <button onClick={() => act('check-out')} className="btn-ghost">Check out</button>
        <button onClick={() => act('cancel')} className="btn-ghost">Cancel</button>
        <button onClick={() => downloadFile(`/bookings/${b.id}/calendar`, `booking-${b.id}.ics`)} className="btn-ghost">Add to calendar</button>
        <Link to={`/book/${b.resources?.[0]?.id}`} className="btn-ghost">Book again</Link>
      </div>
    </div>
  )
}
