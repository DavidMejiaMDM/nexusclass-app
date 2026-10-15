package com.marcosmejia.nexusclass.ui.screens.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcosmejia.nexusclass.ui.theme.AcentoCielo
import com.marcosmejia.nexusclass.ui.theme.Ambar
import com.marcosmejia.nexusclass.ui.theme.AzulUA
import com.marcosmejia.nexusclass.ui.theme.AzulUAOscuro
import com.marcosmejia.nexusclass.ui.theme.RojoClaro
import com.marcosmejia.nexusclass.ui.theme.RojoError
import com.marcosmejia.nexusclass.ui.theme.TextoPrincipal
import com.marcosmejia.nexusclass.ui.theme.VerdeExito

private val FormaCampo = RoundedCornerShape(14.dp)

// =====================================================================
//  PANTALLAS
// =====================================================================

@Composable
fun LoginScreen(onRegistro: () -> Unit, vm: AuthViewModel = viewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val foco = LocalFocusManager.current

    MarcoAuth(compacta = false) {
        Titulo("Bienvenido de nuevo", "Ingresa con tu correo institucional para ver tu horario y tus tareas.")

        CampoTexto(
            valor = email,
            onCambio = { email = it; vm.quitarError("email") },
            etiqueta = "Correo institucional",
            icono = Icons.Outlined.Email,
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

        AvisoError(ui.errorGeneral)

        BotonPrincipal("Entrar", ui.cargando) {
            foco.clearFocus()
            vm.entrar(email, password)
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        EnlaceCambio("¿No tienes cuenta?", "Regístrate", onRegistro)
    }
}

@Composable
fun RegistroScreen(onLogin: () -> Unit, vm: AuthViewModel = viewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var nombre by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    val foco = LocalFocusManager.current

    MarcoAuth(compacta = true) {
        Titulo("Crea tu cuenta", "Así tu horario y tus tareas quedan guardados solo para ti.")

        AvisoDominio()

        CampoTexto(
            valor = nombre,
            onCambio = { nombre = it; vm.quitarError("nombre") },
            etiqueta = "Nombre completo",
            icono = Icons.Outlined.Person,
            error = ui.errores["nombre"],
            capitalizacion = KeyboardCapitalization.Words
        )
        CampoTexto(
            valor = email,
            onCambio = { email = it; vm.quitarError("email") },
            etiqueta = "Correo institucional",
            icono = Icons.Outlined.Email,
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
        IndicadorFuerza(password)
        CampoClave(
            valor = confirmar,
            onCambio = { confirmar = it; vm.quitarError("confirmar") },
            etiqueta = "Repite la contraseña",
            error = ui.errores["confirmar"],
            imeAction = ImeAction.Done,
            onAccion = { vm.registrar(nombre, email, password, confirmar) }
        )

        AvisoError(ui.errorGeneral)

        BotonPrincipal("Crear cuenta", ui.cargando) {
            foco.clearFocus()
            vm.registrar(nombre, email, password, confirmar)
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        EnlaceCambio("¿Ya tienes cuenta?", "Inicia sesión", onLogin)
    }
}

// =====================================================================
//  MARCO: fondo con degradado a pantalla completa y contenido centrado
// =====================================================================

@Composable
private fun MarcoAuth(compacta: Boolean, contenido: @Composable ColumnScope.() -> Unit) {
    BarraDeEstadoClara()

    // Entrada suave: aparece con un leve desplazamiento hacia arriba
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alfa by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(500))
    val desplazamiento by animateDpAsState(targetValue = if (visible) 0.dp else 24.dp, animationSpec = tween(500))

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AzulUA, AzulUAOscuro)))
            .drawBehind {
                // Círculos decorativos translúcidos
                drawCircle(
                    Color.White.copy(alpha = 0.07f),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.95f, size.height * 0.04f)
                )
                drawCircle(
                    Color.White.copy(alpha = 0.05f),
                    radius = size.width * 0.45f,
                    center = Offset(size.width * 0.02f, size.height * 0.97f)
                )
                drawCircle(
                    Color.White.copy(alpha = 0.04f),
                    radius = size.width * 0.2f,
                    center = Offset(size.width * 0.85f, size.height * 0.8f)
                )
            }
            .imePadding()
    ) {
        val altoDisponible = maxHeight

        // El contenido mide al menos lo que mide la pantalla: si es corto se centra,
        // si es largo (registro, teclado abierto) se puede desplazar.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = altoDisponible)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .graphicsLayer {
                    alpha = alfa
                    translationY = desplazamiento.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoApp(if (compacta) 68.dp else 88.dp)
            Spacer(Modifier.height(14.dp))
            Text(
                "Nexus Class",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Tu mejor aliado",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(if (compacta) 20.dp else 28.dp))

            Surface(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    content = contenido
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Corporación Universitaria Autónoma del Cauca",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Usa tu logo (res/drawable/logo_app) si existe; si no, muestra un ícono de respaldo
@Composable
private fun LogoApp(tamano: Dp) {
    val contexto = LocalContext.current
    val recurso = remember {
        contexto.resources.getIdentifier("logo_app", "drawable", contexto.packageName)
    }
    // Aro translúcido alrededor del círculo blanco
    Box(
        modifier = Modifier
            .size(tamano + 20.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(tamano)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (recurso != 0) {
                Image(
                    painter = painterResource(recurso),
                    contentDescription = "Logo de Organizador UA",
                    modifier = Modifier.size(tamano * 0.68f)
                )
            } else {
                Icon(
                    Icons.Outlined.School,
                    contentDescription = null,
                    tint = AzulUA,
                    modifier = Modifier.size(tamano * 0.5f)
                )
            }
        }
    }
}

// Todo el fondo es azul oscuro: los íconos de las barras del sistema deben verse claros
// mientras esta pantalla esté abierta (al salir se restauran).
@Composable
private fun BarraDeEstadoClara() {
    val vista = LocalView.current
    DisposableEffect(Unit) {
        val ventana = vista.context.buscarActividad()?.window
        val control = ventana?.let { WindowCompat.getInsetsController(it, vista) }
        val estadoAnterior = control?.isAppearanceLightStatusBars
        val navegacionAnterior = control?.isAppearanceLightNavigationBars
        control?.isAppearanceLightStatusBars = false
        control?.isAppearanceLightNavigationBars = false
        onDispose {
            if (control != null && estadoAnterior != null) control.isAppearanceLightStatusBars = estadoAnterior
            if (control != null && navegacionAnterior != null) control.isAppearanceLightNavigationBars = navegacionAnterior
        }
    }
}

private fun Context.buscarActividad(): Activity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

// =====================================================================
//  PIEZAS
// =====================================================================

@Composable
private fun Titulo(titulo: String, subtitulo: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            titulo,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            subtitulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    teclado: KeyboardType = KeyboardType.Text,
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.None
) {
    val foco = LocalFocusManager.current
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        singleLine = true,
        shape = FormaCampo,
        isError = error != null,
        supportingText = if (error != null) ({ Text(error) }) else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = teclado,
            capitalization = capitalizacion,
            imeAction = ImeAction.Next
        ),
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
        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
        singleLine = true,
        shape = FormaCampo,
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

// Botón con degradado azul (se ve bien en tema claro y oscuro)
@Composable
private fun BotonPrincipal(texto: String, cargando: Boolean, onClick: () -> Unit) {
    val forma = RoundedCornerShape(16.dp)
    Button(
        onClick = { if (!cargando) onClick() },
        shape = forma,
        contentPadding = PaddingValues(),
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(AzulUA, AcentoCielo)), forma),
            contentAlignment = Alignment.Center
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(texto, style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    }
}

@Composable
private fun EnlaceCambio(pregunta: String, accion: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(
            buildAnnotatedString {
                append(pregunta)
                append(" ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(accion) }
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AvisoDominio() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            "Solo pueden registrarse personas con correo institucional ${AuthViewModel.DOMINIO}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AvisoError(mensaje: String?) {
    if (mensaje == null) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RojoClaro)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = RojoError)
        Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = TextoPrincipal)
    }
}

// Medidor de seguridad de la contraseña (solo orienta: el requisito real es 8 caracteres)
@Composable
private fun IndicadorFuerza(password: String) {
    if (password.isEmpty()) return
    val nivel = nivelClave(password)
    val (texto, color) = when (nivel) {
        0, 1 -> "Débil" to RojoError
        2 -> "Aceptable" to Ambar
        3 -> "Buena" to AcentoCielo
        else -> "Fuerte" to VerdeExito
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (i < nivel.coerceAtLeast(1)) color
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
        Text("Seguridad: $texto", style = MaterialTheme.typography.bodySmall, color = color)
    }
}

private fun nivelClave(p: String): Int {
    var puntos = 0
    if (p.length >= 8) puntos++
    if (p.any { it.isDigit() } && p.any { it.isLetter() }) puntos++
    if (p.any { it.isUpperCase() } && p.any { it.isLowerCase() }) puntos++
    if (p.any { !it.isLetterOrDigit() } || p.length >= 12) puntos++
    return puntos
}