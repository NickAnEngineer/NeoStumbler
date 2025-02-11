package xyz.malkki.neostumbler.http

import android.content.Context
import kotlin.io.path.createDirectories
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import xyz.malkki.neostumbler.BuildConfig
import xyz.malkki.neostumbler.R

object HttpUtils {
    /** HTTP cache size in bytes */
    const val CACHE_SIZE: Long = 25 * 1024 * 1024

    val CONNECT_TIMEOUT = 30.seconds
    /* Read timeout should be long enough, because the Geosubmit API responds only when all data has been processed and
    that might take a while if a large amount of reports is sent at once */
    val READ_TIMEOUT = 2.minutes

    fun getUserAgent(context: Context): String {
        val userAgentVersion =
            if (BuildConfig.DEBUG) {
                "dev"
            } else {
                BuildConfig.VERSION_CODE
            }

        val appName = context.resources.getString(R.string.app_name).substringBefore(" (")

        return "${appName}/${userAgentVersion}"
    }

    fun createOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                addInterceptor(UserAgentInterceptor(getUserAgent(context)))

                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor(Timber::d).apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                        }
                    )
                }

                connectTimeout(CONNECT_TIMEOUT.toJavaDuration())
                readTimeout(READ_TIMEOUT.toJavaDuration())

                val cacheDir =
                    context.cacheDir.toPath().resolve("okhttp_cache").apply { createDirectories() }

                cache(Cache(cacheDir.toFile(), CACHE_SIZE))
            }
            .build()
    }
}
