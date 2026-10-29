package com.example.githubui

import com.example.githubui.navigation.AppNavigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.githubui.ui.theme.GithubuiTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GithubuiTheme {

                AppNavigation()

            }
        }
    }
}
