import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import client from '../api/client'
import { errorMessage } from '../utils/status'

export default function Approvals() {
  const [rows, setRows] = useState([])
  const load = () => client.get('/approvals').then((r) => setRows(r.data))
  useEffect(() => { load() }, [])

  const decide = async (id, path) => {
    const comment = path === 'reject' ? window.prompt('Reason') : ''
    try {
      await client.post(`/bookings/${id}/${path}`, { comment })
      toast.success(path === 'approve' ? 'Approved' : 'Rejected')
      load()
    } catch (err) {
      toast.error(errorMessage(err))
    }
  }

  return (
    <div>
      <h1 className="font-display text-4xl">Pending approvals</h1>
      <div className="mt-4 space-y-3">
        {rows.map((b) => (
          <div key={b.id} className="soft-card p-4">
            <div className="font-semibold">{b.resources?.[0]?.name} · {b.date} {b.startTime}–{b.endTime}</div>
            <div className="text-sm">Purpose: {b.purpose}</div>
            <div className="text-xs">{b.userName} · {b.status}</div>
            <div className="mt-3 flex gap-2">
              <button onClick={() => decide(b.id, 'approve')} className="rounded-full bg-grove-700 px-4 py-1.5 text-white">Approve</button>
              <button onClick={() => decide(b.id, 'reject')} className="rounded-full border px-4 py-1.5">Reject</button>
            </div>
          </div>
        ))}
        {rows.length === 0 && <p>Nothing waiting for you.</p>}
      </div>
    </div>
  )
}
