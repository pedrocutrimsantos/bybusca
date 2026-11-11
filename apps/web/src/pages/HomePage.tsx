import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import SearchBox from '../components/SearchBox'
import ResultList from '../components/ResultList'
import Filters from '../components/Filters'
import GoogleCsePanel from '../components/GoogleCsePanel'
import ImageSearchPanel from '../components/ImageSearchPanel'
import AppHeader from '../components/AppHeader'
import BrandLogo from '../components/BrandLogo'
import { search, type SearchHit } from '../api/client'

const QUICK_FILTERS = [
  { id: 'ALL', label: 'Buscar na escola' },
  { id: 'COLLECTION', label: 'Acervo' },
  { id: 'WEB', label: 'Web educacional' },
  { id: 'IMAGE', label: 'Imagens' },
] as const

export default function HomePage() {
  const [params, setParams] = useSearchParams()
  const qParam = params.get('q') || ''
  const typeParam = params.get('type') || 'ALL'
  const [q, setQ] = useState(qParam)
  const [type, setType] = useState(typeParam)
  const [items, setItems] = useState<SearchHit[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string>()
  const [warnings, setWarnings] = useState<string[]>([])
  const [tookMs, setTookMs] = useState<number>()
  const userName = sessionStorage.getItem('bybusca_user')
  const showImages = typeParam === 'IMAGE'
  const showInternal = typeParam !== 'WEB' && typeParam !== 'IMAGE'
  const showCse = typeParam === 'WEB' || typeParam === 'ALL'
  const hasQuery = Boolean(qParam)

  useEffect(() => {
    setQ(qParam)
    setType(typeParam)
  }, [qParam, typeParam])

  useEffect(() => {
    if (!qParam || !showInternal) {
      setItems([])
      setWarnings([])
      setTookMs(undefined)
      setLoading(false)
      setError(undefined)
      return
    }
    let cancelled = false
    setLoading(true)
    setError(undefined)
    search(qParam, typeParam === 'ALL' ? undefined : typeParam, false)
      .then((res) => {
        if (cancelled) return
        setItems(res.items.filter((i) => i.source === 'internal'))
        setWarnings(res.warnings || [])
        setTookMs(res.tookMs)
      })
      .catch((e: Error) => {
        if (!cancelled) setError(e.message)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [qParam, typeParam, showInternal])

  function runSearch(nextValue?: string, nextType = type) {
    const next = (nextValue ?? q).trim()
    if (!next) return
    setQ(next)
    setType(nextType)
    setParams({ q: next, type: nextType })
  }

  function changeType(id: string) {
    setType(id)
    if (qParam) setParams({ q: qParam, type: id })
  }

  function quickFilter(id: string) {
    setType(id)
    const next = q.trim()
    if (next) {
      setParams({ q: next, type: id })
    } else if (id === 'IMAGE') {
      setParams({ type: id })
    }
  }

  function goImageSearch() {
    const next = q.trim()
    setType('IMAGE')
    if (next) {
      setParams({ q: next, type: 'IMAGE' })
    } else {
      setParams({ type: 'IMAGE' })
    }
  }

  function handleLogout() {
    sessionStorage.removeItem('bybusca_token')
    sessionStorage.removeItem('bybusca_user')
    window.location.href = '/'
  }

  return (
    <div className={hasQuery ? 'page page--results' : 'page page--home'}>
      <AppHeader
        userName={userName}
        onLogout={handleLogout}
        tone={hasQuery ? 'brand' : 'minimal'}
      />

      <main>
        {!hasQuery && typeParam !== 'IMAGE' ? (
          <section className="hero">
            <BrandLogo variant="hero" linked={false} />
            <SearchBox
              value={q}
              onChange={setQ}
              onSubmit={(v) => runSearch(v, type)}
              autoFocus
              variant="hero"
              onImageSearch={goImageSearch}
            />
            <div className="hero-actions" role="group" aria-label="Atalhos de busca">
              {QUICK_FILTERS.map((f) => (
                <button
                  key={f.id}
                  type="button"
                  className={`hero-actions__btn${type === f.id ? ' hero-actions__btn--active' : ''}`}
                  onClick={() => quickFilter(f.id)}
                >
                  {f.label}
                </button>
              ))}
            </div>
            <p className="hero-footnote">Busca educacional com SafeSearch · acervo e web da escola</p>
          </section>
        ) : (
          <section className="search-top">
            <SearchBox value={q} onChange={setQ} onSubmit={(v) => runSearch(v, type)} />
            <Filters active={type} onChange={changeType} />
            {showImages ? <ImageSearchPanel query={qParam} /> : null}
            {showInternal && qParam ? (
              <>
                {tookMs != null && !loading ? (
                  <p className="meta-line">
                    Escola · cerca de {items.length} resultados ({tookMs} ms)
                  </p>
                ) : null}
                <ResultList
                  items={items}
                  loading={loading}
                  error={error}
                  warnings={warnings}
                  query={qParam}
                />
              </>
            ) : null}
            {showCse && qParam ? <GoogleCsePanel query={qParam} variant="results" /> : null}
          </section>
        )}
      </main>
    </div>
  )
}
