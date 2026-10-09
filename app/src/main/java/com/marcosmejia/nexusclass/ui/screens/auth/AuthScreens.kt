package com.marcosmejia.nexusclass.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcosmejia.nexusclass.ui.theme.RojoError

@Composable
fun LoginScreen(onRegistro: () -> Unit, vm: AuthViewModel = viewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Marco("Inicia sesión", "Entra para ver tu horario y tus tareas") {
        CampoTexto(
            valor = email,
            onCambio = { email = it; vm.quitarError("email") },
            etiqueta = "Correo electrónico",
            error = ui.errores["email"],
            teclado = KeyboardType.Email
        )
        CampoClave(
            valor = password,
            onCambio = { password = it; vm.quitarError("password") },
            etiqueta = "Contraseña",
            error = ui.errores["password"],
            imeAction = ImeAction.Done,
            onAccion = { vm.entrar(email, password) }
        )
        ui.errorGeneral?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = RojoError) }

        BotonPrincipal("Entrar", ui.cargando) { vm.entrar(email, password) }
        TextButton(onClick = onRegistro, modifier = Modifier.fillMaxWidth()) {
            Text("¿No tienes cuenta? Crear cuenta")
        }
    }
}

@Composable
fun RegistroScreen(onLogin: () -> Unit, vm: AuthViewModel = viewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var nombre by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }

    Marco("Crea tu cuenta", "Así tu horario y tus tareas quedan solo para ti") {
        CampoTexto(
            valor = nombre,
            onCambio = { nombre = it; vm.quitarError("nombre") },
            etiqueta = "Nombre",
            error = ui.errores["nombre"],
            capitalizacion = KeyboardCapitalization.Words
        )
        CampoTexto(
            valor = email,
            onCambio = { email = it; vm.quitarError("email") },
            etiqueta = "Correo electrónico",
            error = ui.errores["email"],
            teclado = KeyboardType.Email
        )
        CampoClave(
            valor = password,
            onCambio = { password = it; vm.quitarError("password") },
            etiqueta = "Contraseña",
            error = ui.errores["password"],
            ayuda = "Mínimo 8 caracteres",
            imeAction = ImeAction.Next
        )
        CampoClave(
            valor = confirmar,
            onCambio = { confirmar = it; vm.quitarError("confirmar") },
            etiqueta = "Repite la contraseña",
            error = ui.errores["confirmar"],
            imeAction = ImeAction.Done,
            onAccion = { vm.registrar(nombre, email, password, confirmar) }
        )
        ui.errorGeneral?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = RojoError) }

        BotonPrincipal("Crear cuenta", ui.cargando) { vm.registrar(nombre, email, password, confirmar) }
        TextButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}

// ---------- Piezas compartidas ----------

@Composable
private fun Marco(titulo: String, subtitulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("ORGANIZADOR UA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(titulo, style = MaterialTheme.typography.headlineMedium)
        Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        contenido()
    }
}

@Composable
private fun BotonPrincipal(texto: String, cargando: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = !cargando, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        if (cargando) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp) else Text(texto)
    }
}

@Composable
private fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    error: String?,
    teclado: KeyboardType = KeyboardType.Text,
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.None
) {
    val foco = LocalFocusManager.current
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error != null,
        supportingText = if (error != null) ({ Text(error) }) else null,
        keyboardOptions = KeyboardOptions(keyboardType = teclado, capitalization = capitalizacion, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { foco.moveFocus(FocusDirection.Down) }),
        modifier = Modifier.fillMaxWidth()
    )
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