import { useEffect, useRef, useState, type ChangeEvent, type FormEvent } from 'react'
import {
  searchImages,
  searchByImageFile,
  searchByImageUrl,
  type ImageHit,
} from '../api/client'

const WARNING_MESSAGES: Record<string, string> = {
  PSE_UNAVAILABLE: 'Busca de imagens na web indisponível no momento.',
  VISION_UNAVAILABLE: 'Busca por foto indisponível (Vision desligada ou sem chave).',
  IMAGE_BLOCKED: 'Esta imagem não pode ser usada nesta busca.',
}

function mapWarning(code: string): string {
  return WARNING_MESSAGES[code] ?? code
}

type Props = {
  query: string
}

export default function ImageSearchPanel({ query }: Props) {
  const [items, setItems] = useState<ImageHit[]>([])
  const [loading, setLoading] = useState(false)
  const [reverseLoading, setReverseLoading] = useState(false)
  const [error, setError] = useState<string>()
  const [warnings, setWarnings] = useState<string[]>([])
  const [imageUrl, setImageUrl] = useState('')
  const fileInputRef = useRef<HTMLInputElement>(null)
  const trimmedQuery = query.trim()

  useEffect(() => {
    if (!trimmedQuery) {
      setItems([])
      setWarnings([])
      setError(undefined)
      setLoading(false)
      return
    }
    let cancelled = false
    setLoading(true)
    setError(undefined)
    searchImages(trimmedQuery)
      .then((res) => {
        if (cancelled) return
        setItems(res.items)
        setWarnings(res.warnings || [])
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
  }, [trimmedQuery])

  async function handleFileChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setReverseLoading(true)
    setError(undefined)
    try {
      const res = await searchByImageFile(file)
      setItems(res.items)
      setWarnings(res.warnings || [])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha na busca por imagem')
    } finally {
      setReverseLoading(false)
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }

  async function handleUrlSubmit(e: FormEvent) {
    e.preventDefault()
    const url = imageUrl.trim()
    if (!url) return
    setReverseLoading(true)
    setError(undefined)
    try {
      const res = await searchByImageUrl(url)
      setItems(res.items)
      setWarnings(res.warnings || [])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha na busca por URL de imagem')
    } finally {
      setReverseLoading(false)
    }
  }

  const isLoading = loading || reverseLoading

  return (
    <section className="image-search-panel" aria-label="Busca de imagens">
      <div className="image-reverse">
        <h2 className="image-section-title">Busca por imagem</h2>
        <p className="hint">
          Envie uma foto ou informe a URL de uma imagem para encontrar páginas semelhantes.
        </p>
        <div className="image-reverse-actions">
          <label className="image-file-label">
            <span className="image-file-btn">Escolher arquivo</span>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png,image/webp"
              onChange={handleFileChange}
              disabled={isLoading}
              className="sr-only"
            />
          </label>
          <form className="image-url-form" onSubmit={handleUrlSubmit}>
            <input
              type="url"
              placeholder="URL da imagem (https://…)"
              value={imageUrl}
              onChange={(ev) => setImageUrl(ev.target.value)}
              disabled={isLoading}
            />
            <button type="submit" disabled={isLoading || !imageUrl.trim()}>
              Buscar
            </button>
          </form>
        </div>
      </div>

      {warnings.length > 0 && (
        <div className="image-warnings">
          {warnings.map((w) => (
            <p key={w} className="warn">
              {mapWarning(w)}
            </p>
          ))}
        </div>
      )}

      {error ? <p className="state error">{error}</p> : null}

      {isLoading ? (
        <div className="image-grid-loading" aria-busy="true">
          <div className="skeleton image-skeleton" />
          <div className="skeleton image-skeleton" />
          <div className="skeleton image-skeleton" />
          <div className="skeleton image-skeleton" />
        </div>
      ) : null}

      {!isLoading && items.length > 0 ? (
        <div className="image-results">
          {trimmedQuery ? (
            <p className="meta-line">Resultados para “{trimmedQuery}”</p>
          ) : null}
          <ul className="image-grid">
            {items.map((item, idx) => {
              const thumb = item.thumbnailUrl || item.imageUrl
              return (
                <li key={`${item.pageUrl}-${idx}`}>
                  <a href={item.pageUrl} target="_blank" rel="noopener noreferrer">
                    <img src={thumb} alt={item.title} loading="lazy" />
                    <span className="image-title">{item.title}</span>
                  </a>
                </li>
              )
            })}
          </ul>
        </div>
      ) : null}

      {!isLoading && !error && trimmedQuery && items.length === 0 ? (
        <p className="state">Nenhuma imagem encontrada para “{trimmedQuery}”.</p>
      ) : null}
    </section>
  )
}
