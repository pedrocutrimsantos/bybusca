import { Link } from 'react-router-dom'

type Props = {
  variant?: 'hero' | 'header' | 'compact'
  linked?: boolean
}

function Wordmark({ variant }: { variant: 'header' | 'compact' }) {
  return (
    <span className={`brand-wordmark brand-wordmark--${variant}`} aria-hidden="true">
      <span className="brand-wordmark__by">By</span>
      <span className="brand-wordmark__busca">Busca</span>
    </span>
  )
}

export default function BrandLogo({ variant = 'header', linked = true }: Props) {
  const isHero = variant === 'hero'

  const content = isHero ? (
    <img
      src="/logo-bybusca.png"
      alt="ByBusca — busca da escola"
      className="brand-logo brand-logo--hero"
      width={280}
      height={140}
      decoding="async"
    />
  ) : (
    <Wordmark variant={variant === 'compact' ? 'compact' : 'header'} />
  )

  if (!linked) {
    return (
      <div className="brand-mark" aria-label="ByBusca">
        {content}
      </div>
    )
  }

  return (
    <Link to="/" className="brand-logo-link" aria-label="ByBusca — início">
      {content}
    </Link>
  )
}
