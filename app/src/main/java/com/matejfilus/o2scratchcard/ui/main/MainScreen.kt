package com.matejfilus.o2scratchcard.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.matejfilus.o2scratchcard.viewmodel.ScratchCardViewModel
import com.matejfilus.o2scratchcard.domain.model.CardState
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun MainScreen(navController: NavController, viewModel: ScratchCardViewModel) {
    val card by viewModel.card.collectAsState()
    val history by viewModel.history.collectAsState()

    val scale = remember { Animatable(1f) }

    // color according to condition
    val codeColor = when (card.state) {
        CardState.ACTIVATED -> Color(0xFF2E7D32)
        CardState.SCRATCHED -> Color.Black
        else -> Color.Gray
    }

    // animation
    LaunchedEffect(card.code) {
        if (card.code != null) {
            scale.snapTo(0.8f)
            scale.animateTo(
                1f,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = { overshootInterpolator(it) }
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top frame with current status and code
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current state: ${card.state}",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(visible = card.code != null) {
                    Card(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Card code:",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = card.code ?: "",
                                color = codeColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.scale(scale.value)
                            )
                        }
                    }
                }

            }
        }


        Spacer(modifier = Modifier.height(12.dp))

        // History – stretched between the top and bottom
        if (history.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Scratch Card History",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history) { item ->
                            val formattedTime = remember(item.timestamp) {
                                java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                                    .format(java.util.Date(item.timestamp))
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = item.code ?: "(no code)",
                                            color = when (item.state) {
                                                CardState.ACTIVATED -> Color(0xFF2E7D32)
                                                CardState.SCRATCHED -> Color.Black
                                                else -> Color.Gray
                                            },
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.state.name,
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Text(
                                        text = formattedTime,
                                        fontSize = 13.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scratch button
            Button(
                onClick = { navController.navigate("scratch") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2),
                    contentColor = Color.White
                )
            ) {
                Text("Go to Scratch")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Activation button
            val isActivated = card.state == CardState.ACTIVATED
            Button(
                onClick = { navController.navigate("activation") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isActivated,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActivated) Color(0xFFBDBDBD) else Color(0xFF2E7D32),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE0E0E0),
                    disabledContentColor = Color.DarkGray
                )
            ) {
                Text(
                    text = if (isActivated)
                        "Already Activated"
                    else
                        "Go to Activation"
                )
            }
        }

    }
}

// Interpolation
fun overshootInterpolator(t: Float): Float {
    val tension = 4f
    val t2 = t - 1.0f
    return t2 * t2 * ((tension + 1) * t2 + tension) + 1.0f
}
