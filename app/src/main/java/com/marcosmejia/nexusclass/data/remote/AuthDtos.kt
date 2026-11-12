package com.marcosmejia.nexusclass.data.remote

data class RegistroRequest(val nombre: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class UsuarioDto(val id: String, val nombre: String, val email: String)
data class AuthResponse(val token: String, val usuario: UsuarioDto)

//datos para cambio de nombre y contraseña
data class MensajeResponse(val mensaje: String)

data class PerfilRequest(val nombre: String)

data class PerfilResponse(val usuario: UsuarioDto)

data class CambioPasswordRequest(val passwordActual: String, val passwordNueva: String)

data class OlvidePasswordRequest(val email: String)

data class OlvideResponse(val mensaje: String, val reenviarEnSegundos: Int = 60)

data class RestablecerRequest(val email: String, val codigo: String, val passwordNueva: String)