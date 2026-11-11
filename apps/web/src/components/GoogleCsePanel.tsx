import { useEffect, useId, useRef } from 'react'
import { GOOGLE_PSE_CX, GOOGLE_PSE_PUBLIC_URL } from '../config/pse'

declare global {
  interface Window {
    __gcse?: {
      parsetags?: string
      callback?: () => void
    }
    google?: {
      search?: {
        cse?: {
          element?: {
            render: (options: {
              div: string
              tag: string
              gname: string
              attributes?: Record<string, string>
            }) => void
            getElement: (gname: string) => { execute: (q: string) => void } | null
          }
        }
      }
    }
  }
}

type Props = {
  query?: string
  variant?: 'box' | 'results'
}

/**
 * Widget Google CSE.
 * Query limpa (sem -porn/-apostas): SafeSearch + Sites to search no painel já filtram.
 * Operadores negativos longos zeravam ou distorciam buscas como “wikipedia”.
 */
export default function GoogleCsePanel({ query, variant = 'results' }: Props) {
  const reactId = useId().replace(/:/g, '')
  const gname = `bybusca-${variant}-${reactId}`
  const divId = `gcse-${gname}`
  const ready = useRef(false)
  const lastQuery = useRef('')

  function runQuery(raw?: string) {
    const q = raw?.trim()
    const cse = window.google?.search?.cse?.element
    if (!cse || !q) return
    lastQuery.current = q
    // CSE às vezes ainda não registrou o gname no primeiro tick
    const tryExec = (attempt: number) => {
      const el = cse.getElement(gname)
      if (el) {
        el.execute(q)
        return
      }
      if (attempt < 8) {
        window.setTimeout(() => tryExec(attempt + 1), 120 * (attempt + 1))
      }
    }
    tryExec(0)
  }

  useEffect(() => {
    const boot = () => {
      const cse = window.google?.search?.cse?.element
      if (!cse) return
      const el = document.getElementById(divId)
      if (!el) return
      el.innerHTML = ''
      cse.render({
        div: divId,
        tag: variant === 'box' ? 'searchbox-only' : 'searchresults-only',
        gname,
        attributes: {
          safeSearch: 'active',
          linkTarget: '_blank',
          enableImageSearch: 'false',
          disableWebSearch: 'false',
        },
      })
      ready.current = true
      if (variant === 'results' && query?.trim()) {
        runQuery(query)
      }
    }

    window.__gcse = {
      parsetags: 'explicit',
      callback: boot,
    }

    const existing = document.querySelector<HTMLScriptElement>('script[data-bybusca-cse]')
    if (existing) {
      if (window.google?.search?.cse?.element) {
        boot()
      } else {
        existing.addEventListener('load', boot)
      }
      return () => {
        existing.removeEventListener('load', boot)
        ready.current = false
      }
    }

    const script = document.createElement('script')
    script.src = `https://cse.google.com/cse.js?cx=${encodeURIComponent(GOOGLE_PSE_CX)}`
    script.async = true
    script.dataset.bybuscaCse = GOOGLE_PSE_CX
    script.addEventListener('load', () => {
      // callback __gcse costuma bastar; load é fallback
      if (!ready.current) boot()
    })
    document.body.appendChild(script)

    return () => {
      ready.current = false
    }
  }, [divId, gname, variant, query])

  useEffect(() => {
    if (variant !== 'results' || !query?.trim()) return
    if (!ready.current) return
    if (lastQuery.current === query.trim()) return
    runQuery(query)
  }, [query, gname, variant])

  const publicSearch = `${GOOGLE_PSE_PUBLIC_URL}&q=${encodeURIComponent(query?.trim() || '')}`

  return (
    <section className="cse-panel" aria-label="Busca web educacional filtrada">
      <p className="cse-label">
        Web educacional · SafeSearch ativo{' '}
        <a href={publicSearch} target="_blank" rel="noreferrer">
          (abrir engine)
        </a>
      </p>
      <p className="cse-hint">
        Resultados da web vêm do Google CSE (lista educacional). Se esta área ficar vazia,
        confira no painel se <code>*.wikipedia.org</code> está em Sites to search e se
        “Search only included sites” está ativo.
      </p>
      <div id={divId} className={variant === 'box' ? 'gcse-searchbox-only' : 'gcse-searchresults-only'} />
    </section>
  )
}
