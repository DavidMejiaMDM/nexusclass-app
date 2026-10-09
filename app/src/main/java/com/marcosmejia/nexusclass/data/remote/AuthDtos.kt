package com.marcosmejia.nexusclass.data.remote

data class RegistroRequest(val nombre: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class UsuarioDto(val id: String, val nombre: String, val email: String)
data class AuthResponse(val token: String, val usuario: UsuarioDto)
data class MensajeResponse(val mensaje: String)