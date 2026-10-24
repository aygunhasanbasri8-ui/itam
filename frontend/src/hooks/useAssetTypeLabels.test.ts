import { describe, expect, it } from 'vitest'
import type { AssetTypeDefinition } from '../api/assetTypes'
import { activeAssetTypes, mergeAssetTypeLabels } from './useAssetTypeLabels'

function type(code: string, displayName: string, sortOrder: number, active = true): AssetTypeDefinition {
  return { code, displayName, kind: code, icon: '', description: null, system: true, active, sortOrder }
}

describe('asset tipi etiketleri', () => {
  it('istek dönmeden sabit tabloyu kullanıyor', () => {
    expect(mergeAssetTypeLabels(undefined).SERVER).toBe('Sunucu')
    expect(activeAssetTypes(undefined)).toHaveLength(5)
  })

  it('yöneticinin verdiği adı ve sırayı kullanıyor, pasif tipi listelemiyor', () => {
    const types = [type('WORKSTATION', 'Dizüstü', 2), type('SERVER', 'Fiziksel Sunucu', 1), type('CLOUD', 'Bulut', 3, false)]
    expect(mergeAssetTypeLabels(types).SERVER).toBe('Fiziksel Sunucu')
    expect(activeAssetTypes(types).map((t) => t.code)).toEqual(['SERVER', 'WORKSTATION'])
  })
})
