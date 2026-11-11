import { useCallback, useEffect, useState } from 'react'
import { healthIndex, reindex } from '../api/client'
import { GOOGLE_PSE_CX, GOOGLE_PSE_PUBLIC_URL } from '../config/pse'
import AppHeader from '../components/AppHeader'

export default function AdminPage() {
  const [data, setData] = useState<{ tenant: string; documents: number; status: string }>()
  const [error, setError] = useState<string>()
  const [reindexMsg, setReindexMsg] = useState<string>()
  const [busy, setBusy] = useState(false)

  const load = useCallback(() => {
    healthIndex()
      .then(setData)
      .catch((e: Error) => setError(e.message))
  }, [])

  useEffect(() => {
    load()
  }, [load])

  async function handleReindex() {
    setBusy(true)
    setReindexMsg(undefined)
    setError(undefined)
    try {
      const result = await reindex()
      setReindexMsg(`Reindexados ${result.updated} documentos em ${result.tookMs} ms.`)
      load()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Falha ao reindexar')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page page--narrow">
      <AppHeader showAuth={false} />
      <main className="content-panel">
        <h1 className="page-title">Painel admin</h1>
        <p className="tagline">Status do índice, reindexação e orientação de operação.</p>
        {error ? <p className="state error">{error} — entre como admin.</p> : null}
        {data ? (
          <ul className="admin-stats">
            <li>
              Tenant: <strong>{data.tenant}</strong>
            </li>
            <li>
              Documentos ativos: <strong>{data.documents}</strong>
            </li>
            <li>
              Status: <strong>{data.status}</strong>
            </li>
          </ul>
        ) : !error ? (
          <p className="state">Carregando…</p>
        ) : null}

        {data ? (
          <div className="admin-actions">
            <button type="button" onClick={handleReindex} disabled={busy}>
              {busy ? 'Reindexando…' : 'Reindexar tenant'}
            </button>
            {reindexMsg ? <p className="hint">{reindexMsg}</p> : null}
          </div>
        ) : null}

        <section className="help-block">
          <h2>Google Programmable Search</h2>
          <p>
            ID do mecanismo: <code>{GOOGLE_PSE_CX}</code>
          </p>
          <p className="hint">
            URL pública:{' '}
            <a href={GOOGLE_PSE_PUBLIC_URL} target="_blank" rel="noreferrer">
              {GOOGLE_PSE_PUBLIC_URL}
            </a>
          </p>
          <p className="hint">
            No painel CSE: cadastre{' '}
            <strong>Sites to search</strong> (<code>sites-incluidos-cse.txt</code>) e{' '}
            <strong>Sites to exclude</strong> (<code>sites-excluidos-cse.txt</code>), com
            SafeSearch ativo. Inclua o domínio oficial da escola.
          </p>
          <p className="hint">
            Widget no ByBusca já usa este <code>cx</code>. Para JSON API no backend, defina
            também <code>GOOGLE_PSE_API_KEY</code> e <code>GOOGLE_PSE_ENABLED=true</code>.
          </p>
        </section>
      </main>
    </div>
  )
}
