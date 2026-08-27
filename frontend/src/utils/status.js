export function statusMeta(status) {
  switch (status) {
    case 'AVAILABLE':
      return { label: 'Available', color: '#16a34a', emoji: '🟢' }
    case 'BOOKED':
      return { label: 'Booked', color: '#dc2626', emoji: '🔴' }
    case 'MAINTENANCE':
      return { label: 'Maintenance', color: '#ca8a04', emoji: '🟡' }
    case 'BLOCKED':
    case 'OUT_OF_SERVICE':
      return { label: 'Unavailable', color: '#334155', emoji: '⚫' }
    case 'PENDING':
      return { label: 'Pending', color: '#ea580c', emoji: '🟠' }
    default:
      return { label: status || 'Unknown', color: '#64748b', emoji: '⚪' }
  }
}

export function occupancyColor(level) {
  if (level === 'HIGH') return '#dc2626'
  if (level === 'MEDIUM') return '#ca8a04'
  return '#16a34a'
}

export function errorMessage(err, fallback = 'Something went wrong') {
  return err?.response?.data?.message || err?.message || fallback
}
