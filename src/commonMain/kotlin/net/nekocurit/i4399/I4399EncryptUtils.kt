package net.nekocurit.i4399

import korlibs.crypto.AES
import korlibs.crypto.CipherMode.Companion.CBC
import korlibs.crypto.CipherPadding.Companion.PKCS7Padding
import korlibs.crypto.md5
import korlibs.crypto.with
import kotlin.io.encoding.Base64
import kotlin.random.Random

object I4399EncryptUtils {
    fun encrypt(text: String): String {
        val password = "lzYW5qaXVqa".encodeToByteArray()
        val salt = Random.nextBytes(8)

        val keyIv = evpBytesToKey(password, salt, 48)

        val key = keyIv.copyOfRange(0, 32)
        val iv = keyIv.copyOfRange(32, 48)

        val encrypted = AES(key)
            .with(CBC, PKCS7Padding, iv)
            .encrypt(text.encodeToByteArray())

        val out = "Salted__".encodeToByteArray() + salt + encrypted

        return Base64.encode(out)
    }

    fun encrypt2(text: String): String {
        val password = "aqPW2gbMYwz".encodeToByteArray()
        val salt = Random.nextBytes(8)

        val keyIv = evpBytesToKey(password, salt, 48)

        val key = keyIv.copyOfRange(0, 32)
        val iv = keyIv.copyOfRange(32, 48)

        val encrypted = AES(key)
            .with(CBC, PKCS7Padding, iv)
            .encrypt(text.encodeToByteArray())

        val out = "Salted__".encodeToByteArray() + salt + encrypted

        return Base64.encode(out)
    }

    private fun evpBytesToKey(
        password: ByteArray,
        salt: ByteArray,
        size: Int
    ): ByteArray {
        val result = ByteArray(size)

        var prev = ByteArray(0)
        var offset = 0

        while (offset < size) {
            prev = (prev + password + salt).md5().bytes

            val len = minOf(prev.size, size - offset)
            prev.copyInto(result, offset, 0, len)

            offset += len
        }

        return result
    }
}