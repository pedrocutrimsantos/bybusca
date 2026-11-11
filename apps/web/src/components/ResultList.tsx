import type { SearchHit } from '../api/client'

const TYPE_LABEL: Record<string, string> = {
  LESSON: 'Aula',
  COLLECTION: 'Acervo',
  FILE: 'Arquivo',
  PAGE: 'Página',
  MODULE: 'Módulo',
  CONTENT: 'Conteúdo',
  WEB: 'Web',
}

type Props = {
  items: SearchHit[]
  loading: boolean
  error?: string
  warnings?: string[]
  query: string
}

export default function ResultList({ items, loading, error, warnings, query }: Props) {
  if (loading) {
    return (
      <div className="results" aria-busy="true">
        <div className="skeleton" />
        <div className="skeleton" />
        <div className="skeleton" />
      </div>
    )
  }

  if (error) {
    return <p className="state error">{error}</p>
  }

  if (!query) return null

  if (items.length === 0) {
    return (
      <p className="state">
        Nenhum resultado no acervo da escola para “{query}”. Confira a seção{' '}
        <strong>Web educacional</strong> abaixo (Wikipedia, SciELO, MEC…).
      </p>
    )
  }

  return (
    <div className="results">
      {warnings?.includes('PSE_UNAVAILABLE') && (
        <p className="warn">Busca web temporariamente indisponível. Mostrando só o acervo da escola.</p>
      )}
      <ul>
        {items.map((item) => (
          <li key={`${item.source}-${item.id}`}>
            <p className="meta">
              {TYPE_LABEL[item.type] || item.type}
              {item.source === 'pse' ? ' · Google PSE' : ''}
            </p>
            <a href={item.url} target="_blank" rel="noreferrer">
              {item.title}
            </a>
            <p className="snippet" dangerouslySetInnerHTML={{ __html: item.snippet }} />
            <p className="url">{item.url}</p>
          </li>
        ))}
      </ul>
    </div>
  )
}
