package com.example.invernaderoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavegacion()
            }
        }
    }
}

@Composable
fun AppNavegacion() {
    var estaLogueado by remember { mutableStateOf(false) }

    if (!estaLogueado) {
        PantallaLogin(onLoginExitoso = { estaLogueado = true })
    } else {
        PantallaInvernadero()
    }
}

@Composable
fun PantallaLogin(onLoginExitoso: () -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Acceso Administrador", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Usuario (ej: admin)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (error) {
            Text(text = "Credenciales incorrectas", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                // Validación local (simulando SQLite / credenciales de admin)
                if (usuario == "admin" && password == "1234") {
                    onLoginExitoso()
                } else {
                    error = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }
    }
}

@Composable
fun PantallaInvernadero() {
    var temperatura by remember { mutableStateOf("--") }
    var humedad by remember { mutableStateOf("--") }
    val coroutineScope = rememberCoroutineScope()

    // URL de tu ESP32 en Wokwi
    val urlEsp32 = "http://192.168.1.50/datos"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Monitoreo de Invernadero", fontSize = 22.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Temperatura: $temperatura °C", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Humedad: $humedad %", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val url = URL(urlEsp32)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val temp = jsonObject.getDouble("temperatura")
                    val hum = jsonObject.getDouble("humedad")

                    withContext(Dispatchers.Main) {
                        temperatura = temp.toString()
                        humedad = hum.toString()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }) {
            Text(text = "Actualizar Datos del Sensor")
        }
    }
}