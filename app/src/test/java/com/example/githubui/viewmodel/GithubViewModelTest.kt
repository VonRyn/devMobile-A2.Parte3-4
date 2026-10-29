package com.example.githubui.viewmodel

import android.content.Context
import androidx.room.Room
import com.example.githubui.db.GithubDatabase
import com.example.githubui.db.dto.*
import com.example.githubui.repository.GithubRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class GithubViewModelTest {
    private lateinit var context: Context
    private lateinit var banco: GithubDatabase
    private lateinit var repository: GithubRepository
    private lateinit var model: GithubViewModel
    private val arquivos = mutableListOf<String>()

    @Before fun preparar() {
        context = RuntimeEnvironment.getApplication()
        banco = Room.inMemoryDatabaseBuilder(context, GithubDatabase::class.java).build()
        repository = GithubRepository(banco)
        model = GithubViewModel(repository)
    }

    @After fun fechar() {
        banco.close()
        arquivos.forEach { context.deleteDatabase(it) }
    }

    @Test fun bancoReabertoPreservaCadastrosEdicaoStatusEHistorico() = runBlocking {
        banco.close()
        val nome = "persistencia-${UUID.randomUUID()}"
        arquivos.add(nome)
        fun abrir() = Room.databaseBuilder(context, GithubDatabase::class.java, nome)
            .addMigrations(GithubDatabase.MIGRATION_1_2).build()
        banco = abrir()
        var dados = GithubRepository(banco)
        val criado = dados.salvarRepositorio(null, "app", "Original", "Kotlin", false).repositorios.single()
        dados.salvarRepositorio(criado.id, "app-editado", "Atualizada", "Java", true)
        val issue = dados.salvarIssue(null, criado.id, "Issue", "Descrição").issues.single()
        dados.alternarIssue(issue.id)
        val antes = dados.carregarDados()
        banco.close()
        banco = abrir()
        dados = GithubRepository(banco)
        assertEquals(antes, dados.carregarDados())
        val reaberto = GithubViewModel(dados)
        reaberto.carregarDados()
        assertTrue(reaberto.carregado)
        assertEquals("app-editado", reaberto.repositorios.single().nome)
        assertTrue(reaberto.issues.single().concluida)
        assertEquals(4, reaberto.atividades.size)
        dados.excluirIssue(issue.id)
        dados.excluirRepositorio(criado.id)
        banco.close()
        banco = abrir()
        val excluido = GithubRepository(banco).carregarDados()
        assertTrue(excluido.repositorios.isEmpty())
        assertTrue(excluido.issues.isEmpty())
        assertEquals(6, excluido.atividades.size)
    }

    @Test fun daoConsultaPorIdERepositorioAtualizaEExclui() = runBlocking {
        val repoDao = banco.repositorioDao()
        val issueDao = banco.issueDao()
        val id = repoDao.inserir(Repositorio(nome = "app", descricao = "", linguagem = "", privado = false)).toInt()
        val outroId = repoDao.inserir(Repositorio(nome = "outro", descricao = "", linguagem = "", privado = false)).toInt()
        val issueId = issueDao.inserir(Issue(repositorioId = id, titulo = "Original", descricao = "")).toInt()
        issueDao.inserir(Issue(repositorioId = outroId, titulo = "Outra", descricao = ""))
        val original = requireNotNull(issueDao.buscarPorId(issueId))
        issueDao.atualizar(original.copy(titulo = "Atualizada"))
        assertEquals("Atualizada", issueDao.listarPorRepositorio(id).single().titulo)
        assertEquals(2, issueDao.listarTodos().size)
        issueDao.excluir(original)
        assertNull(issueDao.buscarPorId(issueId))
        assertTrue(issueDao.listarPorRepositorio(id).isEmpty())
        val repo = requireNotNull(repoDao.buscarPorId(id))
        repoDao.atualizar(repo.copy(nome = "Renomeado"))
        assertEquals("Renomeado", repoDao.buscarPorId(id)?.nome)
        repoDao.excluir(repo)
        assertNull(repoDao.buscarPorId(id))
    }

    @Test fun validacoesNaoGravamEIdsSaoGeradosPeloBanco() = runBlocking {
        assertNotNull(model.salvarRepositorio(null, "", "", "", false))
        assertNotNull(model.salvarRepositorio(null, "a".repeat(101), "", "", false))
        assertNotNull(model.salvarRepositorio(null, "app", "a".repeat(351), "", false))
        assertNull(model.salvarRepositorio(null, " app ", "", "", false))
        val primeiro = model.repositorios.single()
        assertTrue(primeiro.id > 0)
        assertNotNull(model.salvarRepositorio(null, "APP", "", "", false))
        assertEquals(1, model.atividades.size)
        assertNotNull(model.adicionarIssue(999, "Inválida", ""))
        assertNotNull(model.adicionarIssue(primeiro.id, " ", ""))
        assertTrue(model.issues.isEmpty())
        assertNull(model.excluirRepositorio(primeiro.id))
        assertNull(model.salvarRepositorio(null, "novo", "", "", false))
        assertTrue(model.repositorios.single().id > primeiro.id)
    }

    @Test fun historicoPreservaCopiasENaoDuplicaEdicaoSemMudancas() = runBlocking {
        model.salvarRepositorio(null, "primeiro", "Descrição inicial", "Kotlin", false)
        val id = model.repositorios.single().id
        model.salvarRepositorio(id, "primeiro", "Descrição inicial", "Kotlin", false)
        assertEquals(1, model.atividades.size)
        model.adicionarIssue(id, "Original", "Descrição")
        val issueId = model.issues.single().id
        model.editarIssue(issueId, id, "Original", "Descrição")
        assertEquals(2, model.atividades.size)
        model.editarIssue(issueId, id, "Nova", "Nova descrição")
        model.alternarIssue(issueId)
        model.alternarIssue(issueId)
        model.excluirIssue(issueId)
        val eventos = model.atividades.filter { it.issue != null }
        assertEquals(listOf(AcaoAtividade.EXCLUIDO, AcaoAtividade.REABERTO, AcaoAtividade.CONCLUIDO, AcaoAtividade.EDITADO, AcaoAtividade.CRIADO), eventos.map { it.acao })
        assertEquals("Original", eventos.last().issue?.titulo)
        assertEquals("Nova", eventos.first().issue?.titulo)
        assertTrue(eventos[2].issue!!.concluida)
        assertEquals(model.atividades.size, model.atividades.map { it.id }.distinct().size)
    }

    @Test fun excluirRepositorioRemoveSoSuasIssuesEMantemHistorico() = runBlocking {
        model.salvarRepositorio(null, "primeiro", "", "", false)
        model.salvarRepositorio(null, "segundo", "", "", false)
        val primeiro = model.repositorios.first { it.nome == "primeiro" }.id
        val segundo = model.repositorios.first { it.nome == "segundo" }.id
        model.adicionarIssue(primeiro, "Apagar", "")
        model.adicionarIssue(segundo, "Manter", "")
        model.excluirRepositorio(primeiro)
        assertEquals(segundo, model.repositorios.single().id)
        assertEquals("Manter", model.issues.single().titulo)
        val excluidos = model.atividades.filter { it.acao == AcaoAtividade.EXCLUIDO }
        assertEquals(2, excluidos.size)
        assertTrue(excluidos.any { it.issue?.titulo == "Apagar" })
        assertNull(excluidos.first().issue)
    }

    @Test fun editarIssuePreservaIdConclusaoEVinculoAoReabrir() = runBlocking {
        model.salvarRepositorio(null, "a", "", "", false)
        model.salvarRepositorio(null, "b", "", "", false)
        val a = model.repositorios[0].id
        val b = model.repositorios[1].id
        model.adicionarIssue(a, "Original", "")
        val id = model.issues.single().id
        model.alternarIssue(id)
        assertNull(model.editarIssue(id, b, " Novo título ", " Nova descrição "))
        assertEquals(Issue(id, b, "Novo título", "Nova descrição", true), model.issues.single())
        val antes = model.issues.single()
        assertNotNull(model.editarIssue(id, 999, "Inválida", ""))
        assertNotNull(model.editarIssue(id, b, " ", ""))
        assertEquals(antes, model.issues.single())
        model.excluirRepositorio(a)
        assertEquals(id, model.issues.single().id)
    }

    @Test fun detalhesBuscamBancoMesmoSeCacheDaTelaEstiverVazio() = runBlocking {
        model.salvarRepositorio(null, "app", "", "", false)
        val repo = model.repositorios.single()
        model.adicionarIssue(repo.id, "Issue", "Detalhes persistidos")
        val issue = model.issues.single()
        model.repositorios.clear()
        model.issues.clear()
        assertEquals(repo, model.buscarRepositorio(repo.id))
        assertEquals(issue, model.buscarIssue(issue.id))
        assertNull(model.buscarIssue(999))
    }

    @Test fun transacaoDesfazCadastroSeGravacaoDoHistoricoFalhar() = runBlocking {
        banco.openHelper.writableDatabase.execSQL("CREATE TRIGGER falhar_historico BEFORE INSERT ON atividades BEGIN SELECT RAISE(ABORT, 'falha de teste'); END")
        assertNotNull(model.salvarRepositorio(null, "nao-salvar", "", "", false))
        assertTrue(repository.carregarDados().repositorios.isEmpty())
        assertTrue(repository.carregarDados().atividades.isEmpty())
    }

    @Test fun migracaoDaVersao1PreservaRegistrosExistentes() = runBlocking {
        banco.close()
        val nome = "migracao-${UUID.randomUUID()}"
        arquivos.add(nome)
        context.openOrCreateDatabase(nome, Context.MODE_PRIVATE, null).use { antigo ->
            antigo.execSQL("CREATE TABLE repositorios (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nome TEXT NOT NULL, descricao TEXT NOT NULL, linguagem TEXT NOT NULL, privado INTEGER NOT NULL)")
            antigo.execSQL("CREATE TABLE issues (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, repositorioId INTEGER NOT NULL, titulo TEXT NOT NULL, descricao TEXT NOT NULL, concluida INTEGER NOT NULL)")
            antigo.execSQL("INSERT INTO repositorios VALUES (7, 'antigo', 'Preservar', 'Kotlin', 0)")
            antigo.execSQL("INSERT INTO issues VALUES (9, 7, 'Issue antiga', 'Preservar', 1)")
            antigo.version = 1
        }
        banco = Room.databaseBuilder(context, GithubDatabase::class.java, nome)
            .addMigrations(GithubDatabase.MIGRATION_1_2).build()
        val dados = GithubRepository(banco)
        assertEquals("antigo", dados.buscarRepositorio(7)?.nome)
        assertEquals(true, dados.buscarIssue(9)?.concluida)
        assertTrue(dados.carregarDados().atividades.isEmpty())
        val novo = dados.salvarRepositorio(null, "novo", "", "", false).repositorios.first { it.nome == "novo" }
        assertTrue(novo.id > 7)
        assertEquals(1, dados.carregarDados().atividades.size)
    }
}
