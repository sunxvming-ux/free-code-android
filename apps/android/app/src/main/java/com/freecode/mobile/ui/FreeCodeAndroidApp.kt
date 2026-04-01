package com.freecode.mobile.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.freecode.mobile.ui.navigation.FreeCodeNavGraph

@Composable
fun FreeCodeAndroidApp() {
    val navController = rememberNavController()
    MaterialTheme {
        Surface {
            FreeCodeNavGraph(navController = navController)
        }
    }
}
