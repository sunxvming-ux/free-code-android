package com.freecode.mobile.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.freecode.mobile.ui.navigation.FreeCodeNavGraph
import com.freecode.mobile.ui.state.AppViewModel

@Composable
fun FreeCodeAndroidApp() {
    val navController = rememberNavController()
    val viewModel: AppViewModel = viewModel()
    MaterialTheme {
        Surface {
            FreeCodeNavGraph(navController = navController, viewModel = viewModel)
        }
    }
}
