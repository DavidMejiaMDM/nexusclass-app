package com.marcosmejia.nexusclass.ui.screens.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ---------------------------------------------------------------------------
// Paso 1: pedir el código
// ---------------------------------------------------------------------------
@Composable
fun RecuperarScreen(
    onVolver: () -> Unit,
    onCodigoEnviado: (String) -> Unit,
    vm: RecuperarViewModel = viewModel()
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    val foco = LocalFocusManager.current

    MarcoAuth(compacta = false) {
        Titulo(
            "¿Olvidaste tu contraseña?",
            "Escribe tu correo institucional y te enviaremos un código de 6 dígitos para crear una nueva."
        )

        CampoTexto(
            valor = email,
            onCambio = { email = it; vm.quitarError() },
            etiqueta = "Correo institucional",
            icono = Icons.Outlined.Email,
            error = ui.errorEmail,
            teclado = KeyboardType.Email
        )

        AvisoError(ui.errorGeneral)

        BotonPrincipal("Enviar código", ui.enviando) {
            foco.clearFocus()
            vm.enviar(email, onCodigoEnviado)
        }
        EnlaceCambio("¿La recordaste?", "Volver a iniciar sesión", onVolver)
    }
}

// ---------------------------------------------------------------------------
// Paso 2: código + contraseña nueva
// ---------------------------------------------------------------------------
@Composable
fun RestablecerScreen(
    email: String,
    onVolver: () -> Unit,
    vm: RestablecerViewModel = viewModel()
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var codigo by rememberSaveable { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    val foco = LocalFocusManager.current

    // Sin correo (por ejemplo, si el sistema cerró la app por falta de memoria) no hay nada que restablecer
    LaunchedEffect(email) { if (email.isBlank()) onVolver() }

    MarcoAuth(compacta = true) {
        Titulo(
            "Crea tu nueva contraseña",
            "Enviamos un código de 6 dígitos a $email. Revisa también la carpeta de spam; vence en 10 minutos."
        )

        CampoTexto(
            valor = codigo,
            onCambio = { codigo = it.filter(Char::isDigit).take(6); vm.quitarError("codigo") },
            etiqueta = "Código de 6 dígitos",
            icono = Icons.Outlined.Lock,
            error = ui.errores["codigo"],
            teclado = KeyboardType.Number
        )
        CampoClave(
            valor = nueva,
            onCambio = { nueva = it; vm.quitarError("passwordNueva") },
            etiqueta = "Nueva contraseña",
            error = ui.errores["passwordNueva"],
            ayuda = "Mínimo 8 caracteres",
            imeAction = ImeAction.Next
        )
        IndicadorFuerza(nueva)
        CampoClave(
            valor = confirmar,
            onCambio = { confirmar = it; vm.quitarError("confirmar") },
            etiqueta = "Repite la nueva contraseña",
            error = ui.errores["confirmar"],
            imeAction = ImeAction.Done,
            onAccion = { vm.restablecer(email, codigo, nueva, confirmar) }
        )

        AvisoError(ui.errorGeneral)

        BotonPrincipal("Cambiar contraseña", ui.cargando) {
            foco.clearFocus()
            vm.restablecer(email, codigo, nueva, confirmar)
        }

        TextButton(
            onClick = { vm.reenviar(email) },
            enabled = ui.segundos == 0 && !ui.reenviando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (ui.segundos > 0) "Reenviar código en ${ui.segundos} s" else "Reenviar código")
        }
        EnlaceCambio("¿Te equivocaste de correo?", "Volver", onVolver)
    }
}