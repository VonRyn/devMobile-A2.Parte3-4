package com.example.githubui.navigation

import com.example.githubui.ui.components.BottomBar
import com.example.githubui.ui.screens.explorar.ExplorarScreen
import com.example.githubui.ui.screens.home.HomeScreen
import com.example.githubui.ui.screens.issues.CriarIssueScreen
import com.example.githubui.ui.screens.issues.DetalhesIssueScreen
import com.example.githubui.ui.screens.issues.EditarIssueScreen
import com.example.githubui.ui.screens.issues.IssuesScreen
import com.example.githubui.ui.screens.repositorios.CriarRepositorioScreen
import com.example.githubui.ui.screens.repositorios.DetalhesRepositorioScreen
import com.example.githubui.ui.screens.repositorios.EditarRepositorioScreen
import com.example.githubui.ui.screens.repositorios.RepositoriosPrincipaisScreen
import com.example.githubui.ui.screens.repositorios.RepositoriosTopBar
import com.example.githubui.viewmodel.GithubViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.githubui.db.GithubDatabase
import com.example.githubui.repository.GithubRepository
import kotlinx.coroutines.launch
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

object Rotas {
    const val HOME = "home"
    const val EXPLORAR = "explorar"
    const val ISSUES = "issues?repositorioId={repositorioId}"
    const val CRIAR_ISSUE = "criar_issue?repositorioId={repositorioId}"
    const val REPOSITORIOS = "repositorios"
    const val CRIAR = "criar"
    const val EDITAR = "editar/{id}"
    const val EDITAR_ISSUE = "editar_issue/{id}"
    const val DETALHES_REPOSITORIO = "repositorio/{id}"
    const val DETALHES_ISSUE = "issue/{id}"
    fun editar(id: Int) = "editar/$id"
    fun editarIssue(id: Int) = "editar_issue/$id"
    fun repositorio(id: Int) = "repositorio/$id"
    fun issue(id: Int) = "issue/$id"
    fun issues(repositorioId: Int = -1) = "issues?repositorioId=$repositorioId"
    fun criarIssue(repositorioId: Int = -1) = "criar_issue?repositorioId=$repositorioId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val banco = remember(context) {
        GithubDatabase.getInstance(context.applicationContext)
    }
    val factory = remember(banco) { GithubViewModel.factory(GithubRepository(banco)) }
    val model: GithubViewModel = viewModel(factory = factory)
    LaunchedEffect(Unit) { model.carregarDados() }
    if (!model.carregado) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            val erro = model.erroCarregamento
            if (erro == null) CircularProgressIndicator()
            else {
                val scope = rememberCoroutineScope()
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(erro)
                    TextButton(onClick = { scope.launch { model.carregarDados() } }) { Text("Tentar novamente") }
                }
            }
        }
        return
    }

    val navInterno = rememberNavController()
    val backStackEntry by navInterno.currentBackStackEntryAsState()
    val rotaAtual = backStackEntry?.destination?.route
    val titulo = when (rotaAtual) {
        Rotas.CRIAR -> "Criar repositório"
        Rotas.EDITAR -> "Editar repositório"
        Rotas.DETALHES_REPOSITORIO -> "Detalhes do repositório"
        Rotas.DETALHES_ISSUE -> "Detalhes da issue"
        Rotas.REPOSITORIOS -> "Repositórios Principais"
        Rotas.EXPLORAR -> "Explorar"
        Rotas.ISSUES -> "Issues"
        else -> "Home"
    }

    Scaffold(
        topBar = {
            if (rotaAtual == Rotas.REPOSITORIOS) {
                RepositoriosTopBar(onVoltar = { navInterno.popBackStack() })
            } else if (rotaAtual != null && rotaAtual !in listOf(Rotas.HOME, Rotas.EXPLORAR, Rotas.CRIAR, Rotas.CRIAR_ISSUE, Rotas.EDITAR, Rotas.EDITAR_ISSUE, Rotas.DETALHES_REPOSITORIO, Rotas.DETALHES_ISSUE, Rotas.ISSUES)) {
            TopAppBar(title = { Text(titulo) }, navigationIcon = {
                IconButton(onClick = { navInterno.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                }
            })
            }
        },
        bottomBar = {
            if (rotaAtual !in listOf(Rotas.CRIAR, Rotas.CRIAR_ISSUE, Rotas.EDITAR, Rotas.EDITAR_ISSUE)) {
            Surface(color = Color.White) {
                Box(Modifier.navigationBarsPadding()) {
                    BottomBar(
                        onHome = { navInterno.navigate(Rotas.HOME) {
                            popUpTo(Rotas.HOME)
                            launchSingleTop = true
                        } },
                        onExplorar = { navInterno.navigate(Rotas.EXPLORAR) {
                            popUpTo(Rotas.HOME)
                            launchSingleTop = true
                        } }
                    )
                }
            }
            }
        }
    ) { padding ->
        NavHost(
            navInterno,
            startDestination = Rotas.HOME,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(Rotas.HOME) {
                HomeScreen(
                    onRepositorios = { navInterno.navigate(Rotas.REPOSITORIOS) },
                    onIssues = { navInterno.navigate(Rotas.issues()) },
                    onCriarRepositorio = { navInterno.navigate(Rotas.CRIAR) },
                    onCriarIssue = { navInterno.navigate(Rotas.criarIssue()) },
                    exibirBarraInferior = false
                )
            }
            composable(Rotas.REPOSITORIOS) { RepositoriosPrincipaisScreen(model, { navInterno.navigate(Rotas.CRIAR) }, { navInterno.navigate(Rotas.repositorio(it)) }) }
            composable(Rotas.EXPLORAR) {
                ExplorarScreen(model,
                    onRepositorio = { navInterno.navigate(Rotas.repositorio(it)) },
                    onIssue = { navInterno.navigate(Rotas.issue(it)) })
            }
            composable(Rotas.ISSUES, arguments = listOf(
                navArgument("repositorioId") { type = NavType.IntType; defaultValue = -1 }
            )) { entry ->
                IssuesScreen(model,
                    repositorioId = entry.arguments?.getInt("repositorioId")?.takeIf { it >= 0 },
                    onCriar = { navInterno.navigate(Rotas.criarIssue(entry.arguments?.getInt("repositorioId") ?: -1)) },
                    onVoltar = { navInterno.popBackStack() },
                    onDetalhes = { navInterno.navigate(Rotas.issue(it)) })
            }
            composable(Rotas.CRIAR) {
                CriarRepositorioScreen(model, onFechar = { navInterno.popBackStack() }, onSalvo = { navInterno.popBackStack() })
            }
            composable(Rotas.CRIAR_ISSUE, arguments = listOf(navArgument("repositorioId") { type = NavType.IntType; defaultValue = -1 })) { entry ->
                CriarIssueScreen(model,
                    repositorioId = entry.arguments?.getInt("repositorioId")?.takeIf { it >= 0 },
                    onFechar = { navInterno.popBackStack() },
                    onSalvo = { id -> navInterno.navigate(Rotas.issues(id)) {
                        popUpTo(Rotas.CRIAR_ISSUE) { inclusive = true }
                        launchSingleTop = true
                    } })
            }
            composable(Rotas.EDITAR, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
                EditarRepositorioScreen(model, entry.arguments?.getInt("id") ?: -1, onFechar = { navInterno.popBackStack() }, onSalvo = { navInterno.popBackStack() })
            }
            composable(Rotas.EDITAR_ISSUE, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
                EditarIssueScreen(model, entry.arguments?.getInt("id") ?: -1, onFechar = { navInterno.popBackStack() }, onSalvo = { navInterno.popBackStack() })
            }
            composable(Rotas.DETALHES_REPOSITORIO, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
                DetalhesRepositorioScreen(model, entry.arguments?.getInt("id") ?: -1,
                    onEditar = { navInterno.navigate(Rotas.editar(it)) },
                    onIssues = { navInterno.navigate(Rotas.issues(it)) },
                    onExcluido = { navInterno.popBackStack() },
                    onVoltar = { navInterno.popBackStack() },
                    onNovaIssue = { navInterno.navigate(Rotas.criarIssue(it)) })
            }
            composable(Rotas.DETALHES_ISSUE, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
                DetalhesIssueScreen(model, entry.arguments?.getInt("id") ?: -1,
                    onRepositorio = { navInterno.navigate(Rotas.repositorio(it)) },
                    onExcluido = { navInterno.popBackStack() },
                    onEditar = { navInterno.navigate(Rotas.editarIssue(it)) },
                    onVoltar = { navInterno.popBackStack() })
            }
        }
    }
}
