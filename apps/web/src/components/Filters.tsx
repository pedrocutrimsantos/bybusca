const FILTERS = [
  { id: 'ALL', label: 'Tudo' },
  { id: 'COLLECTION', label: 'Acervo' },
  { id: 'LESSON', label: 'Aulas' },
  { id: 'FILE', label: 'Arquivos' },
  { id: 'PAGE', label: 'Páginas' },
  { id: 'MODULE', label: 'Módulos' },
  { id: 'WEB', label: 'Web' },
  { id: 'IMAGE', label: 'Imagens' },
]

type Props = {
  active: string
  onChange: (id: string) => void
}

export default function Filters({ active, onChange }: Props) {
  return (
    <nav className="filters" aria-label="Filtros por tipo">
      {FILTERS.map((f) => (
        <button
          key={f.id}
          type="button"
          className={active === f.id ? 'active' : ''}
          onClick={() => onChange(f.id)}
        >
          {f.label}
        </button>
      ))}
    </nav>
  )
}
