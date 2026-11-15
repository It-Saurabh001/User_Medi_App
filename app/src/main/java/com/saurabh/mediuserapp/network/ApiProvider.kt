package com.saurabh.mediuserapp.network

import android.content.Context
import android.util.Log
import com.google.gson.annotations.SerializedName
import com.saurabh.mediuserapp.BuildConfig
import com.saurabh.mediuserapp.data.v1.api.AuthApi
import com.saurabh.mediuserapp.data.v1.core.AppEvents
import com.saurabh.mediuserapp.data.v1.core.V1Gson
import com.saurabh.mediuserapp.utils.BASE_URL1
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton




@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthApiService

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainApiService

/** v1 Retrofit (base `.../api/v1/`) on the shared client, with TokenAuthenticator. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class V1Retrofit

/** v1 Retrofit for auth calls: shared client WITHOUT authenticator (used by the refresh flow). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class V1AuthRetrofit

@Module
@InstallIn(SingletonComponent::class)
object ApiProvider {

    const val BASE_URL1 = BuildConfig.WirelessPhysicalDevice

    /** BuildConfig base + `api/v1/` with exactly one slash between and a trailing slash. */
    val V1_BASE_URL: String = BASE_URL1.trimEnd('/') + "/api/v1/"

    private val V1_CONNECT_TIMEOUT_S: Long = if (BuildConfig.DEBUG) 15 else 90


    // Provide TokenManager
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        Log.d("PERF_TRACE", "Hilt provideTokenManager [Thread: ${Thread.currentThread().name}]")
        return TokenManager.getInstance(context)
    }

    // Provide Custom Logging Interceptor
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): LoggingInterceptor {
        return LoggingInterceptor()
    }

    // Provide Logging Interceptor
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Log.d("OkHttp", LoggingInterceptor.redact(message))
        }.apply {
            redactHeader("Authorization")
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
    }

    // Temporary Retrofit for Token Refresh (without auth to avoid circular dependency)
    @Provides
    @Singleton
    @AuthRetrofit
    fun provideTempRetrofit(
        loggingInterceptor: HttpLoggingInterceptor,
        customLoggingInterceptor: LoggingInterceptor
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(customLoggingInterceptor)
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL1)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Temporary ApiService for Token Refresh
    @Provides
    @Singleton
    @AuthApiService
    fun provideTempApiService(@AuthRetrofit retrofit: Retrofit): ApiServices {
        return retrofit.create(ApiServices::class.java)
    }

    // Provide AuthInterceptor
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    // Provide TokenAuthenticator
    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenManager: TokenManager,
        @AuthApiService authApiServiceProvider: Lazy< ApiServices>,
        v1AuthApi: Lazy<AuthApi>,
        appEvents: AppEvents
    ): TokenAuthenticator {
        return TokenAuthenticator(tokenManager, authApiServiceProvider, v1AuthApi, appEvents)
    }

    // Main OkHttpClient with Auth
    @Provides
    @Singleton
    fun provideHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        customLoggingInterceptor: LoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        versionInterceptor: VersionInterceptor,
        errorEventInterceptor: ErrorEventInterceptor
    ): OkHttpClient {
        Log.d("PERF_TRACE", "Hilt provideHttpClient [Thread: ${Thread.currentThread().name}]")
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(customLoggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(versionInterceptor) // v1 paths only
            .addInterceptor(errorEventInterceptor) // v1 paths only
            .authenticator(tokenAuthenticator)
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    // Main Retrofit with Auth
    @Provides
    @Singleton
    @MainRetrofit
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        Log.d("PERF_TRACE", "Hilt provideRetrofit [Thread: ${Thread.currentThread().name}]")
        return Retrofit.Builder()
            .baseUrl(BASE_URL1)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Main ApiService
    @Provides
    @Singleton
    @MainApiService
    fun provideApiServices(@MainRetrofit retrofit: Retrofit): ApiServices {
        return retrofit.create(ApiServices::class.java)
    }

    // ---------------- v1 (same OkHttp stack via newBuilder) ----------------

    @Provides
    @Singleton
    @V1Retrofit
    fun provideV1Retrofit(okHttpClient: OkHttpClient): Retrofit {
        val client = okHttpClient.newBuilder()
            .connectTimeout(V1_CONNECT_TIMEOUT_S, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(V1_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(V1Gson.instance))
            .build()
    }

    @Provides
    @Singleton
    @V1AuthRetrofit
    fun provideV1AuthRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val client = okHttpClient.newBuilder()
            .authenticator(Authenticator.NONE)
            .connectTimeout(V1_CONNECT_TIMEOUT_S, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(V1_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(V1Gson.instance))
            .build()
    }

    @Provides
    @Singleton
    fun provideV1AuthApi(@V1AuthRetrofit retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
}
