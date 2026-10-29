# GitHub UI — A2 Parte 3: Persistência com Room

**Autor:** Christian Gustavo Von Ryn  
**Tema:** aplicativo acadêmico inspirado no GitHub, feito em Kotlin e Jetpack Compose.

Nesta terceira entrega, repositórios, issues e histórico passam a ser gravados em um banco SQLite local com Room. O aplicativo mantém as telas da parte 2 e carrega os dados ao abrir novamente, inclusive depois do encerramento do processo.

## Evolução

- **Parte 1:** três telas visuais e dados fixos.
- **Parte 2:** navegação real, formulários, listas reativas e histórico em memória.
- **Parte 3:** inserir, consultar, atualizar e excluir pelo Room; consultas dos detalhes por id; histórico persistido; migração do banco inicial.

**Video:** https://youtube.com/shorts/HPTCUDP6Mkw

## Organização e decisões

```text
com.example.githubui/
├── MainActivity.kt
├── db/
│   ├── GithubDatabase.kt       # Singleton, versão e migração
│   ├── Conversores.kt         # AcaoAtividade ↔ String
│   ├── dto/                   # Repositorio, Issue e Atividade são @Entity
│   └── dao/                   # Consultas e operações suspend
├── repository/
│   └── GithubRepository.kt    # Regras, transações e Dispatchers.IO
├── viewmodel/
│   └── GithubViewModel.kt     # Estado da interface e resultados das operações
├── navigation/
│   └── AppNavigation.kt       # Factory, carga inicial e rotas
└── ui/
    ├── components/
    ├── preview/
    ├── screens/
    └── theme/
```

O nome do pacote `dto` foi mantido para acompanhar a organização do projeto. As classes anotadas com `@Entity` representam as tabelas do Room.

| Tabela | Campos e motivo |
| --- | --- |
| `repositorios` | Id automático, nome, descrição, linguagem e visibilidade: dados apresentados e editados nas telas existentes. |
| `issues` | Id automático, `repositorioId`, título, descrição e conclusão: cada issue pertence a um repositório. |
| `atividades` | Id automático, ação, horário e cópias dos campos do repositório e da issue: o histórico deve sobreviver à edição ou exclusão do item original. |

`@Embedded` transforma as cópias dos objetos em colunas com prefixos `repo_` e `issue_`. O conversor grava a ação do enum como texto. O histórico não tem chave estrangeira para os itens originais, pois apagá-los não deve apagar a atividade.

`RepositorioDao` e `IssueDao` têm `@Insert`, `@Query`, `@Update` e `@Delete` com `suspend`. Ambos consultam por id, e `IssueDao` também consulta por repositório. `AtividadeDao` insere e lista os eventos.



## Como funciona

```text
Formulário → coroutine → ViewModel → Repository → DAO → Room/SQLite
                                     ↓
                             nova leitura do banco
                                     ↓
                           listas reativas da interface
```

As listas `mutableStateListOf` continuam existindo como cópias para renderização. O banco é a fonte dos registros. Não há mais contadores manuais para gerar ids.

O Repository usa `withContext(Dispatchers.IO)` para as operações de banco. O ViewModel recebe o Repository por uma Factory criada na navegação. A carga inicial usa `LaunchedEffect(Unit)`. As telas de detalhes e de edição usam `LaunchedEffect` com id e revisão dos dados para consultar o registro correto no DAO; as listas não são a fonte dos detalhes.

Os formulários chamam as funções suspend usando `rememberCoroutineScope().launch`. Eles aguardam o resultado, mostram erros sem fechar e desabilitam a confirmação durante a gravação. O ViewModel retorna `null` em caso de sucesso ou uma mensagem em caso de falha. Cancelamentos de coroutine são propagados.

Cada alteração, seu registro de histórico e a leitura atualizada acontecem em uma transação. Excluir um repositório remove suas issues e registra os eventos na mesma transação. Isso é garantido pelo fluxo do Repository, sem uma chave estrangeira nas tabelas atuais. O nome do repositório é validado contra o banco ignorando maiúsculas; uma issue só pode ser salva com um repositório existente. Salvar sem mudanças não duplica o histórico.

## Banco inicial e migração

`GithubDatabase` usa Singleton com `@Volatile` e `synchronized`, nome local `github_database` e versão 2. A versão 1, criada na primeira etapa da integração, já tinha repositórios e issues. `MIGRATION_1_2` acrescenta somente a tabela de atividades, preservando os cadastros anteriores. Não é utilizada migração destrutiva.

O esquema do Room é exportado para `app/schemas/`. Dados que existiam apenas em memória na Parte 2 não podem ser recuperados depois de encerrado o processo.

## Dificuldades técnicas da transição

| Mudança | Solução |
| --- | --- |
| Uma gravação agora é assíncrona. | Os formulários aguardam a conclusão antes de navegar e bloqueiam envios repetidos. |
| Os ids não podem continuar sendo gerados pelo ViewModel. | Novos itens usam id 0 e os DAOs retornam o id criado pelo SQLite. |
| As listas da tela podem estar vazias ou antigas. | Detalhes e edição buscam por id no banco e exibem carregamento/erro. |
| O histórico contém outros objetos. | `@Embedded` grava cópias em colunas e um conversor armazena o enum da ação. |
| Uma exclusão afeta várias tabelas. | Repository executa remoção das issues, remoção do repositório e histórico em transação. |
| A primeira versão do banco já pode existir no aparelho. | Migração 1 → 2 preserva os registros e acrescenta a tabela nova. |

Esses são os problemas técnicos tratados na implementação. O autor deve complementar o relato com as próprias dificuldades de aprendizado antes da entrega. O trabalho é individual; não há divisão de tarefas entre três integrantes.

## Executar e testar

Abra o projeto no Android Studio, sincronize o Gradle e execute `app` em Android 7.0/API 24 ou superior. A configuração atual usa SDK 37 e JDK 25 no daemon do Gradle.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

Os testes locais usam Room com SQLite no ambiente Android do Robolectric. Cobrem CRUD dos DAOs, consultas por id/repositório, ids automáticos, validação, histórico, atualização de issue, exclusão vinculada, rollback da transação, migração 1 → 2 e fechamento/reabertura de um banco em arquivo. Eles não substituem o teste de Forçar parada e a gravação da interface em um aparelho.

APK de depuração: `app/build/outputs/apk/debug/app-debug.apk`.

## Prova de persistência da Parte 3

**Video:** https://youtube.com/shorts/HPTCUDP6Mkw
