package com.saurabh.mediuserapp.network

import android.content.Context
import android.util.Log
import com.google.gson.annotations.SerializedName
import com.saurabh.mediuserapp.BuildConfig
import com.saurabh.mediuserapp.utils.BASE_URL1
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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

@Module
@InstallIn(SingletonComponent::class)
object ApiProvider {

    const val BASE_URL1 = BuildConfig.WirelessPhysicalDevice


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
        return HttpLoggingInterceptor().apply {
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
        @AuthApiService authApiServiceProvider: Lazy< ApiServices>
    ): TokenAuthenticator {
        return TokenAuthenticator(tokenManager, authApiServiceProvider )
    }

    // Main OkHttpClient with Auth
    @Provides
    @Singleton
    fun provideHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        customLoggingInterceptor: LoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        Log.d("PERF_TRACE", "Hilt provideHttpClient [Thread: ${Thread.currentThread().name}]")
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(customLoggingInterceptor)
            .addInterceptor(authInterceptor)
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
}
