package com.matejfilus.o2scratchcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.matejfilus.o2scratchcard.data.repository.DefaultActivationRepository
import com.matejfilus.o2scratchcard.ui.activation.ActivationScreen
import com.matejfilus.o2scratchcard.ui.main.MainScreen
import com.matejfilus.o2scratchcard.ui.scratch.ScratchScreen
import com.matejfilus.o2scratchcard.viewmodel.ScratchCardViewModel
import com.matejfilus.o2scratchcard.viewmodel.ScratchCardViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: ScratchCardViewModel by viewModels {
        ScratchCardViewModelFactory(DefaultActivationRepository(com.matejfilus.o2scratchcard.data.api.RetrofitInstance.api))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            Surface(color = MaterialTheme.colorScheme.background) {
                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        MainScreen(navController = navController, viewModel = viewModel)
                    }
                    composable("scratch") {
                        ScratchScreen(navController = navController, viewModel = viewModel)
                    }
                    composable("activation") {
                        ActivationScreen(navController = navController, viewModel = viewModel)
                    }
                }
            }
        }
    }
}
