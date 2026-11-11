import { Link } from 'react-router-dom'
import { GOOGLE_PSE_CX, GOOGLE_PSE_PUBLIC_URL } from '../config/pse'
import AppHeader from '../components/AppHeader'

export default function HelpPage() {
  return (
    <div className="page page--narrow">
      <AppHeader showAuth={false} />
      <main className="content-panel">
        <h1 className="page-title">Ajuda</h1>
        <p className="tagline">Como usar o mecanismo de busca da escola.</p>

        <section className="help-block">
          <h2>Buscar</h2>
          <ol>
            <li>Digite o tema, disciplina ou nome do material.</li>
            <li>Pressione Enter ou clique em Buscar.</li>
            <li>Use as abas (Aulas, Acervo, Web…) para filtrar.</li>
          </ol>
        </section>

        <section className="help-block">
          <h2>Busca web (Google)</h2>
          <p>
            A aba <strong>Web</strong> usa o Programmable Search da escola com{' '}
            <strong>SafeSearch</strong>, lista de sites educacionais incluídos e exclusão de
            adulto/apostas/streaming — priorizando fontes científicas e escolares.
          </p>
          <p className="hint">
            ID do mecanismo: <code>{GOOGLE_PSE_CX}</code>
          </p>
          <p className="hint">
            URL pública:{' '}
            <a href={GOOGLE_PSE_PUBLIC_URL} target="_blank" rel="noreferrer">
              {GOOGLE_PSE_PUBLIC_URL}
            </a>
          </p>
        </section>

        <section className="help-block">
          <h2>Entrar</h2>
          <p>
            Sem login você vê o catálogo aberto da rede. Com login (aluno/professor/admin),
            materiais da sua turma ou disciplina também aparecem.
          </p>
        </section>

        <section className="help-block">
          <h2>Admin</h2>
          <p>
            Em <Link to="/admin">/admin</Link>, quem tem perfil admin pode ver o status do
            índice e reindexar o acervo.
          </p>
        </section>

        <section className="help-block">
          <h2>Contatos da escola</h2>
          <p className="hint">TI local: __________________</p>
          <p className="hint">Coordenação: __________________</p>
        </section>

        <p className="hint">
          Manual completo: <code>docs/18.9-manual-uso-operacao-suporte</code>
        </p>
      </main>
    </div>
  )
}
