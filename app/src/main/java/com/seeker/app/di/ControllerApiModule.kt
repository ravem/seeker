package com.seeker.app.di

import com.seeker.app.data.meraki.MerakiApiClient
import com.seeker.app.data.meraki.MerakiRepository
import com.seeker.app.data.omada.OmadaApiClient
import com.seeker.app.data.omada.OmadaRepository
import com.seeker.app.data.settings.UserPreferences
import com.seeker.app.data.unifi.UniFiApiClient
import com.seeker.app.data.unifi.UniFiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Dagger che fornisce i client API per i controller di rete (Meraki, UniFi, Omada).
 *
 * I client sono configurati con credenziali vuote di default; verranno riconfigurati
 * a runtime dal ViewModel o dal repository con i dati salvati in UserPreferences.
 */
@Module
@InstallIn(SingletonComponent::class)
object ControllerApiModule {

    @Provides
    @Singleton
    fun provideMerakiApiClient(): MerakiApiClient = MerakiApiClient()

    @Provides
    @Singleton
    fun provideMerakiRepository(
        apiClient: MerakiApiClient
    ): MerakiRepository = MerakiRepository(apiClient)

    @Provides
    @Singleton
    fun provideUniFiApiClient(): UniFiApiClient = UniFiApiClient()

    @Provides
    @Singleton
    fun provideUniFiRepository(
        apiClient: UniFiApiClient
    ): UniFiRepository = UniFiRepository(apiClient)

    @Provides
    @Singleton
    fun provideOmadaApiClient(): OmadaApiClient = OmadaApiClient()

    @Provides
    @Singleton
    fun provideOmadaRepository(
        apiClient: OmadaApiClient
    ): OmadaRepository = OmadaRepository(apiClient)
}
