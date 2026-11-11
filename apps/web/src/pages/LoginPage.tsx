import { FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { login } from '../api/client'
import AppHeader from '../components/AppHeader'

export default function LoginPage() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('aluno')
  const [password, setPassword] = useState('senha123')
  const [error, setError] = useState<string>()
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError(undefined)
    try {
      const res = await login(username, password)
      sessionStorage.setItem('bybusca_token', res.accessToken)
      sessionStorage.setItem('bybusca_user', res.user.displayName)
      navigate('/')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha no login')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page page--narrow">
      <AppHeader showAuth={false} showAdmin={false} />
      <main className="content-panel">
        <h1 className="page-title">Entrar</h1>
        <p className="tagline">Amplie a busca para materiais da sua turma ou disciplina.</p>
        <form className="card-form" onSubmit={onSubmit}>
          <label>
            Usuário
            <input value={username} onChange={(e) => setUsername(e.target.value)} required />
          </label>
          <label>
            Senha
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>
          {error ? <p className="state error">{error}</p> : null}
          <button type="submit" disabled={loading}>
            {loading ? 'Entrando…' : 'Entrar'}
          </button>
          <p className="hint">Demo: admin / prof / aluno — senha <code>senha123</code></p>
        </form>
      </main>
    </div>
  )
}
