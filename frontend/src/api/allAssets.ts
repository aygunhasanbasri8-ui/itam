import { listServers } from './servers'
import { listWorkstations } from './workstations'
import { listClouds } from './clouds'
import { listApplications } from './applications'
import { listBusinessServices } from './businessServices'

export interface AssetOption {
  id: number
  assetName: string
  typeLabel: string
}

// Backend'de tipten bağımsız bir "asset ara" ucu yok; bağımlılık seçimi için
// beş listeyi birleştiriyoruz. Envanter büyürse bunun yerine backend'e
// /assets?name= gibi bir uç eklemek gerekir.
const PICKER_PAGE_SIZE = 500

export async function listAllAssetOptions(): Promise<AssetOption[]> {
  const params = { size: PICKER_PAGE_SIZE }
  const [servers, workstations, clouds, applications, businessServices] = await Promise.all([
    listServers(params),
    listWorkstations(params),
    listClouds(params),
    listApplications(params),
    listBusinessServices(params),
  ])

  const toOptions = (items: { id: number; assetName: string }[], typeLabel: string) =>
    items.map((item) => ({ id: item.id, assetName: item.assetName, typeLabel }))

  return [
    ...toOptions(servers.content, 'Sunucu'),
    ...toOptions(workstations.content, 'İş İstasyonu'),
    ...toOptions(clouds.content, 'Cloud'),
    ...toOptions(applications.content, 'Uygulama'),
    ...toOptions(businessServices.content, 'İş Servisi'),
  ].sort((a, b) => a.assetName.localeCompare(b.assetName, 'tr'))
}
