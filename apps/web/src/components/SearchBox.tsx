import { FormEvent, useEffect, useRef, useState } from 'react'
import { suggest, type SuggestItem } from '../api/client'

type Props = {
  value: string
  onChange: (v: string) => void
  onSubmit: (next?: string) => void
  autoFocus?: boolean
  variant?: 'hero' | 'compact'
  onImageSearch?: () => void
}

function IconSearch() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="2" />
      <path d="M20 20l-3.5-3.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  )
}

function IconCamera() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M4 8h3l1.5-2h7L17 8h3a2 2 0 012 2v8a2 2 0 01-2 2H4a2 2 0 01-2-2v-8a2 2 0 012-2z"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinejoin="round"
      />
      <circle cx="12" cy="13" r="3.5" stroke="currentColor" strokeWidth="2" />
    </svg>
  )
}

export default function SearchBox({
  value,
  onChange,
  onSubmit,
  autoFocus,
  variant = 'compact',
  onImageSearch,
}: Props) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [items, setItems] = useState<SuggestItem[]>([])
  const [open, setOpen] = useState(false)
  const isHero = variant === 'hero'

  useEffect(() => {
    if (autoFocus) inputRef.current?.focus()
  }, [autoFocus])

  useEffect(() => {
    let cancelled = false
    const handle = window.setTimeout(() => {
      suggest(value)
        .then((list) => {
          if (!cancelled) {
            setItems(list)
            setOpen(list.length > 0)
          }
        })
        .catch(() => {
          if (!cancelled) {
            setItems([])
            setOpen(false)
          }
        })
    }, 220)
    return () => {
      cancelled = true
      window.clearTimeout(handle)
    }
  }, [value])

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setOpen(false)
    onSubmit(value)
  }

  function pick(text: string) {
    onChange(text)
    setOpen(false)
    onSubmit(text)
  }

  return (
    <div className={`search-wrap${isHero ? ' search-wrap--hero' : ''}`}>
      <form
        className={`search-box${isHero ? ' search-box--hero' : ''}`}
        onSubmit={handleSubmit}
        role="search"
      >
        {isHero ? (
          <span className="search-box__leading" aria-hidden="true">
            <IconSearch />
          </span>
        ) : null}
        <label className="sr-only" htmlFor="q">
          Buscar na escola
        </label>
        <input
          id="q"
          ref={inputRef}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          onFocus={() => setOpen(items.length > 0)}
          onBlur={() => window.setTimeout(() => setOpen(false), 150)}
          placeholder={isHero ? 'Busque aulas, acervo, arquivos e web…' : 'Busque aulas, acervo, arquivos...'}
          autoComplete="off"
          aria-autocomplete="list"
          aria-expanded={open}
        />
        {isHero ? (
          <div className="search-box__tools">
            {onImageSearch ? (
              <button
                type="button"
                className="search-box__tool"
                title="Buscar por imagem"
                aria-label="Buscar por imagem"
                onClick={onImageSearch}
              >
                <IconCamera />
              </button>
            ) : null}
          </div>
        ) : (
          <button type="submit" className="search-box__submit">
            Buscar
          </button>
        )}
      </form>
      {open ? (
        <ul className="suggest-list" role="listbox">
          {items.map((item) => (
            <li key={`${item.type}-${item.text}`}>
              <button type="button" onMouseDown={() => pick(item.text)}>
                <span>{item.text}</span>
                <small>{item.type}</small>
              </button>
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  )
}
