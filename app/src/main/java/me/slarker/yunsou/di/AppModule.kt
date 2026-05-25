package me.slarker.yunsou.di

import me.slarker.yunsou.data.api.PanSouApi
import me.slarker.yunsou.data.repository.SearchRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSearchRepository(api: PanSouApi): SearchRepository =
        SearchRepository(api)
}
