package com.saurabh.mediuserapp.data.v1.di

import com.saurabh.mediuserapp.data.v1.api.DevicesApi
import com.saurabh.mediuserapp.data.v1.api.HealthApi
import com.saurabh.mediuserapp.data.v1.api.MetaApi
import com.saurabh.mediuserapp.data.v1.api.OrdersApi
import com.saurabh.mediuserapp.data.v1.api.ProductsApi
import com.saurabh.mediuserapp.data.v1.api.UsersApi
import com.saurabh.mediuserapp.network.V1Retrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/** v1 API interfaces on @V1Retrofit. AuthApi is provided in ApiProvider (@V1AuthRetrofit). */
@Module
@InstallIn(SingletonComponent::class)
object V1ApiModule {
    @Provides @Singleton
    fun provideUsersApi(@V1Retrofit retrofit: Retrofit): UsersApi = retrofit.create(UsersApi::class.java)

    @Provides @Singleton
    fun provideProductsApi(@V1Retrofit retrofit: Retrofit): ProductsApi = retrofit.create(ProductsApi::class.java)

    @Provides @Singleton
    fun provideOrdersApi(@V1Retrofit retrofit: Retrofit): OrdersApi = retrofit.create(OrdersApi::class.java)

    @Provides @Singleton
    fun provideDevicesApi(@V1Retrofit retrofit: Retrofit): DevicesApi = retrofit.create(DevicesApi::class.java)

    @Provides @Singleton
    fun provideMetaApi(@V1Retrofit retrofit: Retrofit): MetaApi = retrofit.create(MetaApi::class.java)

    @Provides @Singleton
    fun provideHealthApi(@V1Retrofit retrofit: Retrofit): HealthApi = retrofit.create(HealthApi::class.java)
}
