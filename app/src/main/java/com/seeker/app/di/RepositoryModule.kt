package com.seeker.app.di

import com.seeker.app.data.oui.OuiRepository
import com.seeker.app.data.wifi.WifiRepository
import com.seeker.app.core.util.OuiDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideOuiRepository(
        ouiDatabase: OuiDatabase
    ): OuiRepository = OuiRepository(ouiDatabase)
}
