package io.github.chos1n11111.dongqiudipure.core.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal interface SessionStore {
    suspend fun readAuthorization(): String?
    suspend fun writeAuthorization(value: String)
    suspend fun clear()
}

internal interface DeviceIdStore {
    suspend fun getOrCreate(): String
}

@Singleton
internal class KeystoreSessionStore @Inject constructor(
    @ApplicationContext context: Context,
) : SessionStore {
    private val preferences = context.getSharedPreferences(SESSION_PREFERENCES, Context.MODE_PRIVATE)

    override suspend fun readAuthorization(): String? = withContext(Dispatchers.IO) {
        val encrypted = preferences.getString(CIPHERTEXT_KEY, null) ?: return@withContext null
        val iv = preferences.getString(IV_KEY, null) ?: run {
            clearValues()
            return@withContext null
        }
        runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(TAG_LENGTH_BITS, Base64.decode(iv, Base64.NO_WRAP)),
            )
            String(
                cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)),
                StandardCharsets.UTF_8,
            ).takeIf(String::isNotBlank)
        }.getOrElse {
            clearValues()
            null
        }
    }

    override suspend fun writeAuthorization(value: String) = withContext(Dispatchers.IO) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        check(
            preferences.edit()
                .putString(IV_KEY, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .putString(CIPHERTEXT_KEY, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
                .commit(),
        )
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        clearValues()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
            generateKey()
        }
    }

    private fun clearValues() {
        check(preferences.edit().remove(IV_KEY).remove(CIPHERTEXT_KEY).commit())
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "dongqiudipure.session.authorization"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_LENGTH_BITS = 128
        const val SESSION_PREFERENCES = "encrypted_session"
        const val CIPHERTEXT_KEY = "authorization_ciphertext"
        const val IV_KEY = "authorization_iv"
    }
}

@Singleton
internal class SharedPreferencesDeviceIdStore @Inject constructor(
    @ApplicationContext context: Context,
) : DeviceIdStore {
    private val preferences = context.getSharedPreferences(DEVICE_PREFERENCES, Context.MODE_PRIVATE)

    override suspend fun getOrCreate(): String = withContext(Dispatchers.IO) {
        preferences.getString(UUID_KEY, null)?.takeIf(::isValidUuid)?.let { return@withContext it }

        val generated = UUID.randomUUID().toString()
        check(preferences.edit().putString(UUID_KEY, generated).commit())
        generated
    }

    private fun isValidUuid(value: String): Boolean =
        runCatching { UUID.fromString(value) }.isSuccess

    private companion object {
        const val DEVICE_PREFERENCES = "authenticated_device"
        const val UUID_KEY = "uuid"
    }
}
