package com.marcosmejia.nexusclass.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


object Cifrado {
    private const val ALMACEN = "AndroidKeyStore"
    private const val ALIAS = "nexusclass_sesion_key"
    private const val TRANSFORMACION = "AES/GCM/NoPadding"
    private const val LONGITUD_IV = 12      // bytes
    private const val LONGITUD_TAG = 128    // bits

    private fun almacen(): KeyStore = KeyStore.getInstance(ALMACEN).apply { load(null) }

    // Devuelve la clave existente o crea una nueva la primera vez
    private fun clave(): SecretKey {
        (almacen().getKey(ALIAS, null) as? SecretKey)?.let { return it }

        val generador = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ALMACEN)
        generador.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generador.generateKey()
    }

    // Texto -> "IV + datos cifrados" en Base64. El IV es distinto en cada cifrado.
    fun cifrar(texto: String): String {
        val cipher = Cipher.getInstance(TRANSFORMACION)
        cipher.init(Cipher.ENCRYPT_MODE, clave())
        val datos = cipher.doFinal(texto.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + datos, Base64.NO_WRAP)
    }

    // Lanza excepción si la clave ya no existe o el dato fue alterado (GCM lo detecta)
    fun descifrar(codificado: String): String {
        val bytes = Base64.decode(codificado, Base64.NO_WRAP)
        val iv = bytes.copyOfRange(0, LONGITUD_IV)
        val datos = bytes.copyOfRange(LONGITUD_IV, bytes.size)
        val cipher = Cipher.getInstance(TRANSFORMACION)
        cipher.init(Cipher.DECRYPT_MODE, clave(), GCMParameterSpec(LONGITUD_TAG, iv))
        return String(cipher.doFinal(datos), Charsets.UTF_8)
    }

    // Al cerrar sesión se destruye la clave: cualquier resto cifrado queda ilegible para siempre
    fun borrarClave() {
        runCatching { almacen().deleteEntry(ALIAS) }
    }
}