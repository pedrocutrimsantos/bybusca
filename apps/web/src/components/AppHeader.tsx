import { Link } from 'react-router-dom'
import BrandLogo from './BrandLogo'

type Props = {
  userName?: string | null
  onLogout?: () => void
  showAuth?: boolean
  showAdmin?: boolean
  tone?: 'brand' | 'minimal'
}

export default function AppHeader({
  userName,
  onLogout,
  showAuth = true,
  showAdmin = true,
  tone = 'brand',
}: Props) {
  const minimal = tone === 'minimal'

  return (
    <header className={`app-header${minimal ? ' app-header--minimal' : ''}`}>
      <div className="app-header__inner">
        {!minimal ? <BrandLogo variant="header" /> : <span aria-hidden="true" />}
        <nav className="app-header__nav" aria-label="Navegação principal">
          <Link to="/?type=IMAGE">Imagens</Link>
          {showAdmin ? <Link to="/admin">Admin</Link> : null}
          <Link to="/ajuda">Ajuda</Link>
          {showAuth ? (
            userName ? (
              <>
                <span className="badge">{userName}</span>
                <button type="button" className="linkish" onClick={onLogout}>
                  Sair
                </button>
              </>
            ) : (
              <Link to="/login" className={minimal ? 'app-header__cta' : 'app-header__cta app-header__cta--on-brand'}>
                Entrar
              </Link>
            )
          ) : null}
        </nav>
      </div>
    </header>
  )
}
