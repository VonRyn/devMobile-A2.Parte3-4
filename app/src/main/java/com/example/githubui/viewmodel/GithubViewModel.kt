package com.example.githubui.viewmodel

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.githubui.db.dto.*
import com.example.githubui.repository.EstadoBanco
import com.example.githubui.repository.GithubRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GithubViewModel(private val repository: GithubRepository?) : ViewModel() {
    // São cópias reativas para a interface. Os registros oficiais ficam no Room.
    val repositorios = mutableStateListOf<Repositorio>()
    val issues = mutableStateListOf<Issue>()
    val atividades = mutableStateListOf<Atividade>()
    var ocupado by mutableStateOf(false)
        private set
    var carregado by mutableStateOf(false)
        private set
    var erroCarregamento by mutableStateOf<String?>(null)
        private set
    var revisao by mutableLongStateOf(0L)
        private set
    private val operacoes = Mutex()

    private fun atualizar(estado: EstadoBanco) {
        Snapshot.withMutableSnapshot {
            repositorios.clear()
            repositorios.addAll(estado.repositorios)
            issues.clear()
            issues.addAll(estado.issues)
            atividades.clear()
            atividades.addAll(estado.atividades)
            revisao++
        }
    }

    suspend fun carregarDados() {
        val dados = repository ?: return
        operacoes.withLock {
            ocupado = true
            erroCarregamento = null
            try {
                atualizar(dados.carregarDados())
                carregado = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("RoomError", "Erro ao carregar dados", e)
                erroCarregamento = "Não foi possível carregar os dados. Tente novamente."
            } finally {
                ocupado = false
            }
        }
    }

    // Os detalhes chamam estas consultas por ID, sem usar a lista como fonte.
    suspend fun buscarRepositorio(id: Int): Repositorio? =
        repository?.buscarRepositorio(id) ?: if (repository == null) repositorios.find { it.id == id } else null

    suspend fun buscarIssue(id: Int): Issue? =
        repository?.buscarIssue(id) ?: if (repository == null) issues.find { it.id == id } else null

    private suspend fun executar(operacao: suspend (GithubRepository) -> EstadoBanco): String? {
        val dados = repository ?: return "As alterações estão disponíveis no aplicativo em execução."
        return operacoes.withLock {
            ocupado = true
            try {
                atualizar(operacao(dados))
                null
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                e.message ?: "Confira os dados informados."
            } catch (e: Exception) {
                Log.e("RoomError", "Erro ao salvar alteração", e)
                "Não foi possível salvar a alteração. Tente novamente."
            } finally {
                ocupado = false
            }
        }
    }

    fun validarRepositorio(id: Int?, nome: String, descricao: String): String? {
        val limpo = nome.trim()
        if (limpo.isEmpty()) return "Informe o nome do repositório."
        if (nome.length > 100) return "O nome deve ter no máximo 100 caracteres."
        if (descricao.length > 350) return "A descrição deve ter no máximo 350 caracteres."
        if (!limpo.matches(Regex("[A-Za-z0-9._-]+"))) return "Use letras, números, ponto, hífen ou sublinhado no nome."
        if (repositorios.any { it.id != id && it.nome.equals(limpo, true) }) return "Já existe um repositório com esse nome."
        return null
    }

    suspend fun salvarRepositorio(id: Int?, nome: String, descricao: String, linguagem: String, privado: Boolean): String? =
        executar { it.salvarRepositorio(id, nome, descricao, linguagem, privado) }

    suspend fun excluirRepositorio(id: Int): String? = executar { it.excluirRepositorio(id) }

    suspend fun adicionarIssue(repoId: Int, titulo: String, descricao: String): String? =
        executar { it.salvarIssue(null, repoId, titulo, descricao) }

    suspend fun editarIssue(id: Int, repositorioId: Int, titulo: String, descricao: String): String? =
        executar { it.salvarIssue(id, repositorioId, titulo, descricao) }

    suspend fun alternarIssue(id: Int): String? = executar { it.alternarIssue(id) }

    suspend fun excluirIssue(id: Int): String? = executar { it.excluirIssue(id) }

    companion object {
        fun factory(repository: GithubRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(GithubViewModel::class.java))
                return GithubViewModel(repository) as T
            }
        }
    }
}
