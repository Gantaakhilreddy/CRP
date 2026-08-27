import { motion } from 'framer-motion'

export const fadeUp = {
  initial: { opacity: 0, y: 18 },
  animate: { opacity: 1, y: 0 },
  transition: { duration: 0.45, ease: [0.22, 1, 0.36, 1] },
}

export function MotionCard({ children, className = '', delay = 0 }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.45, delay, ease: [0.22, 1, 0.36, 1] }}
      className={className}
    >
      {children}
    </motion.div>
  )
}

export function StatCard({ label, value, hint, accent }) {
  return (
    <div className="soft-card relative overflow-hidden p-5">
      <div className="stat-kicker">{label}</div>
      <div className="mt-1 font-display text-3xl" style={{ color: accent }}>{value}</div>
      {hint && <div className="mt-1 text-xs text-grove-500">{hint}</div>}
    </div>
  )
}

export function PageHeader({ kicker, title, subtitle, actions }) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        {kicker && <p className="stat-kicker text-gold-500">{kicker}</p>}
        <h1 className="font-display text-4xl tracking-tight">{title}</h1>
        {subtitle && <p className="mt-1 max-w-2xl text-sm text-grove-600 dark:text-grove-200">{subtitle}</p>}
      </div>
      {actions}
    </div>
  )
}
