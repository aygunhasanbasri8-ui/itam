import { useQuery } from '@tanstack/react-query'
import { getDashboardSummary } from '../api/dashboard'
import { ASSET_TYPE_LABELS } from '../types/assetType'

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: 'Aktif',
  RETIRED: 'Hizmet Dışı',
  MAINTENANCE: 'Bakımda',
}

function StatCard({ label, value, accent }: { label: string; value: number | string; accent?: string }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div className="text-sm text-slate-500">{label}</div>
      <div className={`mt-1 text-2xl font-semibold ${accent ?? 'text-slate-800'}`}>{value}</div>
    </div>
  )
}

function DistributionCard({ title, data, labels }: { title: string; data: Record<string, number>; labels?: Record<string, string> }) {
  const entries = Object.entries(data)
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div className="mb-3 text-sm font-medium text-slate-700">{title}</div>
      {entries.length === 0 ? (
        <p className="text-sm text-slate-400">Kayıt yok</p>
      ) : (
        <ul className="space-y-2">
          {entries.map(([key, count]) => (
            <li key={key} className="flex items-center justify-between text-sm">
              <span className="text-slate-600">{labels?.[key] ?? key}</span>
              <span className="font-medium text-slate-800">{count}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export function DashboardPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['dashboard-summary'],
    queryFn: () => getDashboardSummary(30),
  })

  if (isLoading) {
    return <p className="text-slate-500">Yükleniyor...</p>
  }

  if (isError || !data) {
    return <p className="text-red-600">Dashboard verileri alınamadı.</p>
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-800">Dashboard</h1>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatCard label="Toplam Asset" value={data.totalAssets} />
        <StatCard
          label="Süresi Yaklaşan Lisanslar (30 gün)"
          value={data.licensesExpiringSoon}
          accent={data.licensesExpiringSoon > 0 ? 'text-amber-600' : undefined}
        />
        <StatCard
          label="Süresi Geçmiş Lisanslar"
          value={data.licensesExpired}
          accent={data.licensesExpired > 0 ? 'text-red-600' : undefined}
        />
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <DistributionCard title="Asset Tipi Dağılımı" data={data.countByAssetType} labels={ASSET_TYPE_LABELS} />
        <DistributionCard title="Durum Dağılımı" data={data.countByStatus} labels={STATUS_LABELS} />
        <DistributionCard title="Sunucu Ortam Dağılımı" data={data.countByEnvironment} />
      </div>
    </div>
  )
}
