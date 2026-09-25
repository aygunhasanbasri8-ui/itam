export function ComingSoonPage({ title }: { title: string }) {
  return (
    <div className="space-y-2">
      <h1 className="text-xl font-semibold text-slate-800">{title}</h1>
      <p className="text-slate-500">Bu ekran henüz eklenmedi, sonraki bir geliştirme diliminde gelecek.</p>
    </div>
  )
}
