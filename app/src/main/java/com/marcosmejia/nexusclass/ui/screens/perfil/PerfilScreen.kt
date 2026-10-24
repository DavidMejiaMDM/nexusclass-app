package com.marcosmejia.nexusclass.ui.screens.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcosmejia.nexusclass.data.local.Sesion
import com.marcosmejia.nexusclass.ui.components.BarraSuperior
import com.marcosmejia.nexusclass.ui.components.CampoForm
import com.marcosmejia.nexusclass.ui.theme.RojoError

@Composable
fun PerfilScreen(onVolver: () -> Unit, vm: PerfilViewModel = viewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val usuario by Sesion.usuario.collectAsStateWithLifecycle()
    val foco = LocalFocusManager.current

    var nombre by rememberSaveable { mutableStateOf(usuario?.nombre.orEmpty()) }
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }

    val iniciales = usuario?.nombre.orEmpty().trim().split(" ").filter { it.isNotEmpty() }
        .take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "UA" }

    // Al terminar con éxito se vacían los tres campos de contraseña
    val enviarClave = {
        foco.clearFocus()
        vm.cambiarClave(actual, nueva, confirmar) {
            actual = ""
            nueva = ""
            confirmar = ""
        }
    }

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Mi perfil", onVolver)

        Column(
            modifier = Modifier
                .weight(1f)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar con iniciales
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        iniciales,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    usuario?.email.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ---------- Datos personales ----------
            Tarjeta {
                Text("Datos personales", style = MaterialTheme.typography.titleMedium)
                CampoForm(
                    valor = nombre,
                    onCambio = { nombre = it; vm.limpiarErrorNombre() },
                    etiqueta = "Nombre",
                    error = ui.errorNombre
                )
                OutlinedTextField(
                    value = usuario?.email.orEmpty(),
                    onValueChange = {},
                    enabled = false,
                    readOnly = true,
                    singleLine = true,
                    label = { Text("Correo institucional") },
                    supportingText = { Text("El correo no se puede cambiar.") },
                    modifier = Modifier.fillMaxWidth()
                )
                BotonAccion("Guardar nombre", ui.guardandoNombre) {
                    foco.clearFocus()
                    vm.guardarNombre(nombre)
                }
            }

            // ---------- Cambiar contraseña ----------
            Tarjeta {
                Text("Cambiar contraseña", style = MaterialTheme.typography.titleMedium)
                CampoClave(
                    valor = actual,
                    onCambio = { actual = it; vm.quitarErrorClave("passwordActual") },
                    etiqueta = "Contraseña actual",
                    error = ui.erroresClave["passwordActual"],
                    imeAction = ImeAction.Next
                )
                CampoClave(
                    valor = nueva,
                    onCambio = { nueva = it; vm.quitarErrorClave("passwordNueva") },
                    etiqueta = "Nueva contraseña",
                    error = ui.erroresClave["passwordNueva"],
                    ayuda = "Mínimo 8 caracteres",
                    imeAction = ImeAction.Next
                )
                CampoClave(
                    valor = confirmar,
                    onCambio = { confirmar = it; vm.quitarErrorClave("confirmar") },
                    etiqueta = "Repite la nueva contraseña",
                    error = ui.erroresClave["confirmar"],
                    imeAction = ImeAction.Done,
                    onAccion = { enviarClave() }
                )
                ui.errorGeneralClave?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = RojoError)
                }
                Text(
                    "Al cambiarla se cerrarán tus otras sesiones; en este teléfono seguirás dentro.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BotonAccion("Cambiar contraseña", ui.cambiandoClave) { enviarClave() }
            }
        }
    }
}

// ---------- Piezas ----------

@Composable
private fun Tarjeta(contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = contenido
        )
    }
}

@Composable
private fun BotonAccion(texto: String, cargando: Boolean, onClick: () -> Unit) {
    Button(
        onClick = { if (!cargando) onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(texto)
        }
    }
}

@Composable
private fun CampoClave(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    error: String?,
    ayuda: String? = null,
    imeAction: ImeAction,
    onAccion: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    val foco = LocalFocusManager.current
    val apoyo = error ?: ayuda
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error != null,
        supportingText = if (apoyo != null) ({ Text(apoyo) }) else null,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { foco.moveFocus(FocusDirection.Down) },
            onDone = { foco.clearFocus(); onAccion() }
        ),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}