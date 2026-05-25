package me.slarker.yunsou.data.api

import me.slarker.yunsou.data.local.PreferencesManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaseUrlInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val baseUrl = preferencesManager.getBaseUrl()
        val newHttpUrl = baseUrl.toHttpUrlOrNull() ?: return chain.proceed(original)
        val newUrl = original.url.newBuilder()
            .scheme(newHttpUrl.scheme)
            .host(newHttpUrl.host)
            .port(newHttpUrl.port)
            .build()
        return chain.proceed(original.newBuilder().url(newUrl).build())
    }
}
