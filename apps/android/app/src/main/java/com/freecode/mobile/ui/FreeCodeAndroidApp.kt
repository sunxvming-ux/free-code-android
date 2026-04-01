package com.freecode.mobile.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.freecode.mobile.FreeCodeApplication
import com.freecode.mobile.data.OfflineAppRepository
import com.freecode.mobile.ui.navigation.FreeCodeNavGraph
import com.freecode.mobile.ui.state.AppViewModel

@Composable
fun FreeCodeAndroidApp() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext as FreeCodeApplication
    val repository = remember(context) { OfflineAppRepository(context.database) }
    val viewModel: AppViewModel = viewModel(factory = AppViewModel.factory(repository))
    MaterialTheme {
        Surface {
            FreeCodeNavGraph(navController = navController, viewModel = viewModel)
        }
    }
}
