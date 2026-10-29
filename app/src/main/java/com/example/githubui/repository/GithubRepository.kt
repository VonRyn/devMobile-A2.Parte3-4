package com.example.githubui.repository

import androidx.room.withTransaction
import com.example.githubui.db.GithubDatabase
import com.example.githubui.db.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Uma fotografia consistente dos dados para atualizar a interface.
data class EstadoBanco(
    val repositorios: List<Repositorio>,
    val issues: List<Issue>,
    val atividades: List<Atividade>
)

class GithubRepository(private val banco: GithubDatabase) {
    private val repositorioDao = banco.repositorioDao()
    private val issueDao = banco.issueDao()
    private val atividadeDao = banco.atividadeDao()

    private suspend fun lerEstado() = EstadoBanco(
        repositorioDao.listarTodos(), issueDao.listarTodos(), atividadeDao.listarTodos()
    )

    suspend fun carregarDados(): EstadoBanco = withContext(Dispatchers.IO) {
        banco.withTransaction { lerEstado() }
    }

    suspend fun buscarRepositorio(id: Int): Repositorio? = withContext(Dispatchers.IO) {
        repositorioDao.buscarPorId(id)
    }

    suspend fun buscarIssue(id: Int): Issue? = withContext(Dispatchers.IO) {
        issueDao.buscarPorId(id)
    }

    // Alteração, histórico e nova leitura pertencem à mesma transação.
    // Uma falha desfaz tudo, evitando salvar só parte de uma operação.
    private suspend fun alterar(operacao: suspend () -> Unit): EstadoBanco = withContext(Dispatchers.IO) {
        banco.withTransaction {
            operacao()
            lerEstado()
        }
    }

    private suspend fun registrar(repo: Repositorio, acao: AcaoAtividade, issue: Issue? = null) {
        atividadeDao.inserir(Atividade(repositorio = repo, acao = acao, issue = issue))
    }

    suspend fun salvarRepositorio(id: Int?, nome: String, descricao: String, linguagem: String, privado: Boolean): EstadoBanco = alterar {
        val nomeLimpo = nome.trim()
        require(nomeLimpo.isNotEmpty()) { "Informe o nome do repositório." }
        require(nome.length <= 100) { "O nome deve ter no máximo 100 caracteres." }
        require(descricao.length <= 350) { "A descrição deve ter no máximo 350 caracteres." }
        require(nomeLimpo.matches(Regex("[A-Za-z0-9._-]+"))) { "Use letras, números, ponto, hífen ou sublinhado no nome." }
        val duplicado = repositorioDao.buscarPorNome(nomeLimpo)
        require(duplicado == null || duplicado.id == id) { "Já existe um repositório com esse nome." }
        if (id == null) {
            val novo = Repositorio(nome = nomeLimpo, descricao = descricao.trim(), linguagem = linguagem.trim(), privado = privado)
            val gerado = repositorioDao.inserir(novo).toInt()
            registrar(novo.copy(id = gerado), AcaoAtividade.CRIADO)
        } else {
            val anterior = requireNotNull(repositorioDao.buscarPorId(id)) { "Repositório não encontrado." }
            val atualizado = anterior.copy(nome = nomeLimpo, descricao = descricao.trim(), linguagem = linguagem.trim(), privado = privado)
            if (anterior != atualizado) {
                repositorioDao.atualizar(atualizado)
                registrar(atualizado, AcaoAtividade.EDITADO)
            }
        }
    }

    suspend fun excluirRepositorio(id: Int): EstadoBanco = alterar {
        val repo = requireNotNull(repositorioDao.buscarPorId(id)) { "Repositório não encontrado." }
        issueDao.listarPorRepositorio(id).forEach { issue ->
            registrar(repo, AcaoAtividade.EXCLUIDO, issue)
            issueDao.excluir(issue)
        }
        registrar(repo, AcaoAtividade.EXCLUIDO)
        repositorioDao.excluir(repo)
    }

    private fun validarIssue(titulo: String, descricao: String) {
        require(titulo.isNotBlank()) { "Informe o título da issue." }
        require(titulo.length <= 100) { "O título deve ter no máximo 100 caracteres." }
        require(descricao.length <= 350) { "A descrição deve ter no máximo 350 caracteres." }
    }

    suspend fun salvarIssue(id: Int?, repositorioId: Int, titulo: String, descricao: String): EstadoBanco = alterar {
        validarIssue(titulo, descricao)
        val repo = requireNotNull(repositorioDao.buscarPorId(repositorioId)) { "Selecione um repositório válido." }
        if (id == null) {
            val nova = Issue(repositorioId = repositorioId, titulo = titulo.trim(), descricao = descricao.trim())
            val gerado = issueDao.inserir(nova).toInt()
            registrar(repo, AcaoAtividade.CRIADO, nova.copy(id = gerado))
        } else {
            val anterior = requireNotNull(issueDao.buscarPorId(id)) { "Issue não encontrada." }
            val atualizada = anterior.copy(repositorioId = repositorioId, titulo = titulo.trim(), descricao = descricao.trim())
            if (anterior != atualizada) {
                issueDao.atualizar(atualizada)
                registrar(repo, AcaoAtividade.EDITADO, atualizada)
            }
        }
    }

    suspend fun alternarIssue(id: Int): EstadoBanco = alterar {
        val issue = requireNotNull(issueDao.buscarPorId(id)) { "Issue não encontrada." }
        val repo = requireNotNull(repositorioDao.buscarPorId(issue.repositorioId)) { "Repositório não encontrado." }
        val atualizada = issue.copy(concluida = !issue.concluida)
        issueDao.atualizar(atualizada)
        registrar(repo, if (atualizada.concluida) AcaoAtividade.CONCLUIDO else AcaoAtividade.REABERTO, atualizada)
    }

    suspend fun excluirIssue(id: Int): EstadoBanco = alterar {
        val issue = requireNotNull(issueDao.buscarPorId(id)) { "Issue não encontrada." }
        val repo = requireNotNull(repositorioDao.buscarPorId(issue.repositorioId)) { "Repositório não encontrado." }
        registrar(repo, AcaoAtividade.EXCLUIDO, issue)
        issueDao.excluir(issue)
    }
}
