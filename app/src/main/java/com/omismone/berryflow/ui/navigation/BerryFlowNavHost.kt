package com.omismone.berryflow.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.omismone.berryflow.data.BerryFlowRepository
import com.omismone.berryflow.data.Frequency
import com.omismone.berryflow.ui.add.AddScreen
import com.omismone.berryflow.ui.add.AddViewModel
import com.omismone.berryflow.ui.add.AddViewModelFactory
import com.omismone.berryflow.ui.adjustbalance.AdjustBalanceScreen
import com.omismone.berryflow.ui.adjustbalance.AdjustBalanceViewModel
import com.omismone.berryflow.ui.adjustbalance.AdjustBalanceViewModelFactory
import com.omismone.berryflow.ui.categories.CategoriesScreen
import com.omismone.berryflow.ui.categories.CategoriesViewModel
import com.omismone.berryflow.ui.categories.CategoriesViewModelFactory
import com.omismone.berryflow.ui.dashboard.DashboardScreen
import com.omismone.berryflow.ui.dashboard.DashboardViewModel
import com.omismone.berryflow.ui.dashboard.DashboardViewModelFactory
import com.omismone.berryflow.ui.data.DataScreen
import com.omismone.berryflow.ui.insights.InsightsScreen
import com.omismone.berryflow.ui.insights.InsightsViewModel
import com.omismone.berryflow.ui.insights.InsightsViewModelFactory
import com.omismone.berryflow.ui.recurrentevents.RecurrentEventsListScreen
import com.omismone.berryflow.ui.recurrentevents.RecurrentEventsListViewModel
import com.omismone.berryflow.ui.recurrentevents.RecurrentEventsListViewModelFactory
import com.omismone.berryflow.ui.recurrentevents.RecurrentEventsScreen
import com.omismone.berryflow.ui.recurrentevents.RecurrentEventsViewModel
import com.omismone.berryflow.ui.recurrentevents.RecurrentEventsViewModelFactory
import java.time.Instant
import java.time.ZoneId
import com.omismone.berryflow.ui.AppViewModel
import com.omismone.berryflow.ui.AppViewModelFactory

private object Routes {
    const val DASHBOARD = "dashboard"
    const val ADD = "add?transactionId={transactionId}"
    const val INSIGHTS = "insights"
    const val RECURRENT_EVENTS_LIST = "recurrentEventsList"
    const val RECURRENT_EVENT = "recurrentEvent?eventId={eventId}"
    const val ADJUST_BALANCE = "adjustBalance?onboarding={onboarding}"
    const val CATEGORIES = "categories"
    const val DATA = "data"
}

private fun addRoute(transactionId: Long? = null) = "add?transactionId=${transactionId ?: -1}"
private fun recurrentEventRoute(eventId: Long? = null) = "recurrentEvent?eventId=${eventId ?: -1}"
private fun adjustBalanceRoute(onboarding: Boolean = false) = "adjustBalance?onboarding=$onboarding"

@Composable
fun BerryFlowApp(repository: BerryFlowRepository) {
    // Decides the start screen: Adjust Balance (onboarding) if the balance
    // was never set, Dashboard otherwise. null means "still loading", to
    // avoid briefly showing the wrong start screen.
    val isBalanceSet by produceState<Boolean?>(initialValue = null, repository) {
        repository.isBalanceSet.collect { value = it }
    }
    val resolvedIsSet = isBalanceSet ?: return Box(
        modifier = Modifier.fillMaxSize().background(Color.White)
    )

    // Loaded once here (not per-screen) so it's already available by the
    // time any screen needing it is opened.
    val appViewModel: AppViewModel = viewModel(factory = AppViewModelFactory(repository))
    val categories by appViewModel.categories.collectAsState()

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (resolvedIsSet) Routes.DASHBOARD else adjustBalanceRoute(onboarding = true),
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        composable(Routes.DASHBOARD) {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(repository)
            )
            val balance by viewModel.balance.collectAsState()
            val transactions by viewModel.transactions.collectAsState()

            DashboardScreen(
                balance = balance,
                categories = categories,
                transactions = transactions,
                onAddClick = { navController.navigate(addRoute()) },
                onTransactionClick = { navController.navigate(addRoute(it.id)) },
                onInsightsClick = { navController.navigate(Routes.INSIGHTS) },
                onRecurrentEventsClick = { navController.navigate(Routes.RECURRENT_EVENTS_LIST) },
                onCategoriesClick = { navController.navigate(Routes.CATEGORIES) },
                onAdjustBalanceClick = { navController.navigate(adjustBalanceRoute()) },
                onManageDataClick = { navController.navigate(Routes.DATA) }
            )
        }

        composable(
            route = Routes.ADD,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val transactionIdArg = backStackEntry.arguments?.getLong("transactionId") ?: -1L
            val transactionId = transactionIdArg.takeIf { it != -1L }

            val viewModel: AddViewModel = viewModel(
                key = "add-$transactionId",
                factory = AddViewModelFactory(repository, transactionId)
            )
            val editingTransaction by viewModel.editingTransaction.collectAsState()

            if (categories.isEmpty()) return@composable

            fun onSave(amount: Double, name: String, isIncome: Boolean, category: com.omismone.berryflow.data.Category, date: java.time.LocalDate) {
                val dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                viewModel.saveTransaction(amount, name, isIncome, category, dateMillis)
                navController.popBackStack()
            }

            if (transactionId != null) {
                val transaction = editingTransaction ?: return@composable
                val category = categories.firstOrNull { it.id == transaction.categoryId } ?: categories.first()
                AddScreen(
                    categories = categories,
                    initialCategory = category,
                    isEditMode = true,
                    initialAmount = transaction.amount,
                    initialName = transaction.name ?: "",
                    initialIsIncome = transaction.isIncome,
                    initialDate = Instant.ofEpochMilli(transaction.date).atZone(ZoneId.systemDefault()).toLocalDate(),
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = ::onSave,
                    onDeleteClick = {
                        viewModel.deleteTransaction()
                        navController.popBackStack()
                    }
                )
            } else {
                AddScreen(
                    categories = categories,
                    initialCategory = categories.first(),
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = ::onSave
                )
            }
        }

        composable(Routes.INSIGHTS) {
            val viewModel: InsightsViewModel = viewModel(
                factory = InsightsViewModelFactory(repository)
            )
            val categories by viewModel.categories.collectAsState()
            val transactions by viewModel.transactions.collectAsState()

            InsightsScreen(
                categories = categories,
                transactions = transactions,
                onHomeClick = { navController.popBackStack() }
            )
        }

        composable(Routes.RECURRENT_EVENTS_LIST) {
            val viewModel: RecurrentEventsListViewModel = viewModel(
                factory = RecurrentEventsListViewModelFactory(repository)
            )
            val events by viewModel.events.collectAsState()

            RecurrentEventsListScreen(
                events = events,
                categories = categories,
                onHomeClick = { navController.popBackStack() },
                onAddClick = { navController.navigate(recurrentEventRoute()) },
                onEventClick = { navController.navigate(recurrentEventRoute(it.id)) },
                onDeleteEvent = { viewModel.deleteEvent(it) }
            )
        }

        composable(
            route = Routes.RECURRENT_EVENT,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val eventIdArg = backStackEntry.arguments?.getLong("eventId") ?: -1L
            val eventId = eventIdArg.takeIf { it != -1L }

            val viewModel: RecurrentEventsViewModel = viewModel(
                key = "event-$eventId",
                factory = RecurrentEventsViewModelFactory(repository, eventId)
            )
            val editingEvent by viewModel.editingEvent.collectAsState()

            if (categories.isEmpty()) return@composable

            fun onSave(
                amount: Double,
                name: String,
                isIncome: Boolean,
                category: com.omismone.berryflow.data.Category,
                date: java.time.LocalDate,
                frequency: Frequency
            ) {
                val dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                viewModel.saveEvent(amount, name, isIncome, category, dateMillis, frequency)
                navController.popBackStack()
            }

            if (eventId != null) {
                val event = editingEvent ?: return@composable
                val category = categories.firstOrNull { it.id == event.categoryId } ?: categories.first()
                RecurrentEventsScreen(
                    categories = categories,
                    initialCategory = category,
                    isEditMode = true,
                    initialAmount = event.amount,
                    initialName = event.name ?: "",
                    initialIsIncome = event.isIncome,
                    initialDate = Instant.ofEpochMilli(event.startDate).atZone(ZoneId.systemDefault()).toLocalDate(),
                    initialFrequency = Frequency.valueOf(event.frequency),
                    onDiscardClick = { navController.popBackStack() },
                    onSaveClick = ::onSave,
                    onDeleteClick = {
                        viewModel.deleteEvent()
                        navController.popBackStack()
                    }
                )
            } else {
                RecurrentEventsScreen(
                    categories = categories,
                    initialCategory = categories.first(),
                    onDiscardClick = { navController.popBackStack() },
                    onSaveClick = ::onSave
                )
            }
        }

        composable(
            route = Routes.ADJUST_BALANCE,
            arguments = listOf(navArgument("onboarding") { type = NavType.BoolType; defaultValue = false })
        ) { backStackEntry ->
            val isOnboarding = backStackEntry.arguments?.getBoolean("onboarding") ?: false
            val viewModel: AdjustBalanceViewModel = viewModel(
                factory = AdjustBalanceViewModelFactory(repository)
            )
            val currentBalance by viewModel.currentBalance.collectAsState()

            AdjustBalanceScreen(
                currentBalance = currentBalance,
                isOnboarding = isOnboarding,
                onDiscardClick = { navController.popBackStack() },
                onSaveClick = { amount ->
                    viewModel.saveBalance(amount)
                    if (isOnboarding) {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Routes.CATEGORIES) {
            val viewModel: CategoriesViewModel = viewModel(
                factory = CategoriesViewModelFactory(repository)
            )
            val categories by viewModel.categories.collectAsState()

            CategoriesScreen(
                categories = categories,
                onHomeClick = { navController.popBackStack() },
                onAddCategory = { viewModel.addCategory(it) },
                onRenameCategory = { category, newName -> viewModel.renameCategory(category, newName) },
                onRecolorCategory = { category, newColor -> viewModel.recolorCategory(category, newColor) },
                onReemojiCategory = { category, newEmoji -> viewModel.reemojiCategory(category, newEmoji) },
                onDeleteCategory = { viewModel.deleteCategory(it) }
            )
        }

        composable(Routes.DATA) {
            DataScreen(onHomeClick = { navController.popBackStack() })
        }
    }
}