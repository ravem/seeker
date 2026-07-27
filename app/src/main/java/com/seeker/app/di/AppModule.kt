package com.seeker.app.di

import android.content.Context
import com.seeker.app.core.permissions.PermissionManager
import com.seeker.app.core.util.LatencyMonitor
import com.seeker.app.core.util.OuiDatabase
import com.seeker.app.core.util.SpeedTest
import com.seeker.app.data.telephony.MobileNetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePermissionManager(
        @ApplicationContext context: Context
    ): PermissionManager = PermissionManager(context)

    @Provides
    @Singleton
    fun provideOuiDatabase(
        @ApplicationContext context: Context
    ): OuiDatabase = OuiDatabase(context)

    @Provides
    @Singleton
    fun provideLatencyMonitor(): LatencyMonitor = LatencyMonitor()

    @Provides
    @Singleton
    fun provideMobileNetworkMonitor(
        @ApplicationContext context: Context
    ): MobileNetworkMonitor = MobileNetworkMonitor(context)

    @Provides
    @Singleton
    fun provideSpeedTest(): SpeedTest = SpeedTest()
}
