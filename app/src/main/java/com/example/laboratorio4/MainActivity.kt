/*
Estudiante: Allan Vega
Cédula: 8-1001-2089
Salón: 1SF242
* */

package com.example.laboratorio4

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    // Launcher para solicitar permisos
    private val solicitudPermiso =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            results.forEach { (perm, granted) ->
                if (!granted) {
                    Toast.makeText(this, "Permiso $perm rechazado", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        solicitarPermisoLocalizacion()
        setContent {
            MensajeScreen()
        }
    }

    private fun solicitarPermisoLocalizacion() {
        val perms = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            perms += Manifest.permission.ACCESS_FINE_LOCATION
        }
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            perms += Manifest.permission.ACCESS_COARSE_LOCATION
        }
        if (perms.isNotEmpty()) {
            solicitudPermiso.launch(perms.toTypedArray())
        }
    }
}

@Composable
fun MensajeScreen() {
    val context = LocalContext.current
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    val fusedClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextField(
            value = mensaje,
            onValueChange = { mensaje = it },
            label = { Text("Escribe tu mensaje") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                cargando = true
                fusedClient.lastLocation
                    .addOnSuccessListener { loc ->
                        cargando = false
                        val link = loc
                            ?.let { "https://maps.google.com/?q=${it.latitude},${it.longitude}" }
                            ?: "Ubicación no disponible"
                        val text = "$mensaje\n\nMi ubicación: $link"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            setPackage("com.whatsapp")
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(
                                context, "WhatsApp no está instalado", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener {
                        cargando = false
                        Toast.makeText(
                            context, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT
                        ).show()
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Obteniendo ubicación…")
            } else {
                Text("Enviar Mensaje por WhatsApp")
            }
        }
    }
}
