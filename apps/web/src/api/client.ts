const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'
const TENANT = import.meta.env.VITE_TENANT || 'demo-escola'

export type SearchHit = {
  id: string
  type: string
  title: string
  snippet: string
  url: string
  score: number
  source: string
}

export type SearchResponse = {
  items: SearchHit[]
  page: number
  size: number
  total: number
  tookMs: number
  warnings: string[]
}

export type ImageHit = {
  title: string
  pageUrl: string
  imageUrl: string
  thumbnailUrl?: string | null
  score: number
  source: string
}

export type ImageSearchResponse = {
  items: ImageHit[]
  warnings: string[]
}

function tenantAuthHeaders(): Record<string, string> {
  const token = sessionStorage.getItem('bybusca_token')
  const headers: Record<string, string> = { 'X-Tenant': TENANT }
  if (token) headers.Authorization = `Bearer ${token}`
  return headers
}

function authHeader(): HeadersInit {
  return { ...tenantAuthHeaders(), 'Content-Type': 'application/json' }
}

export async function search(q: string, type?: string, includeWeb = false): Promise<SearchResponse> {
  const params = new URLSearchParams({ q, includeWeb: String(includeWeb) })
  if (type && type !== 'ALL') params.set('types', type)
  const res = await fetch(`${API_BASE}/search?${params}`, { headers: authHeader() })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: 'Falha na busca' }))
    throw new Error(err.message || 'Falha na busca')
  }
  return res.json()
}

export async function login(username: string, password: string) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: authHeader(),
    body: JSON.stringify({ username, password }),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: 'Login inválido' }))
    throw new Error(err.message || 'Login inválido')
  }
  return res.json() as Promise<{
    accessToken: string
    user: { displayName: string; roles: string[]; username: string }
  }>
}

export async function healthIndex() {
  const res = await fetch(`${API_BASE}/admin/health-index`, { headers: authHeader() })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: 'Sem acesso ao painel' }))
    throw new Error(err.message || 'Sem acesso ao painel')
  }
  return res.json() as Promise<{ tenant: string; documents: number; status: string }>
}

export async function reindex() {
  const res = await fetch(`${API_BASE}/index/reindex`, {
    method: 'POST',
    headers: authHeader(),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: 'Falha ao reindexar' }))
    throw new Error(err.message || 'Falha ao reindexar')
  }
  return res.json() as Promise<{ tenant: string; updated: number; tookMs: number }>
}

export type SuggestItem = { text: string; type: string }

export async function searchImages(q: string, size = 10): Promise<ImageSearchResponse> {
  const params = new URLSearchParams({ q, size: String(size) })
  const res = await fetch(`${API_BASE}/search/images?${params}`, { headers: authHeader() })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: 'Falha na busca de imagens' }))
    throw new Error(err.message || 'Falha na busca de imagens')
  }
  return res.json()
}

export async function searchByImageFile(file: File): Promise<ImageSearchResponse> {
  const form = new FormData()
  form.append('file', file)
  const res = await fetch(`${API_BASE}/search/image`, {
    method: 'POST',
    headers: tenantAuthHeaders(),
    body: form,
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: 'Falha na busca por imagem' }))
    throw new Error(err.message || 'Falha na busca por imagem')
  }
  return res.json()
}

export async function searchByImageUrl(imageUrl: string): Promise<ImageSearchResponse> {
  const res = await fetch(`${API_BASE}/search/image`, {
    method: 'POST',
    headers: authHeader(),
    body: JSON.stringify({ imageUrl }),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: 'Falha na busca por URL de imagem' }))
    throw new Error(err.message || 'Falha na busca por URL de imagem')
  }
  return res.json()
}

export async function suggest(q: string): Promise<SuggestItem[]> {
  if (q.trim().length < 2) return []
  const params = new URLSearchParams({ q: q.trim() })
  const res = await fetch(`${API_BASE}/suggest?${params}`, { headers: authHeader() })
  if (!res.ok) return []
  return res.json()
}

export function logout() {
  sessionStorage.removeItem('bybusca_token')
  sessionStorage.removeItem('bybusca_user')
}
