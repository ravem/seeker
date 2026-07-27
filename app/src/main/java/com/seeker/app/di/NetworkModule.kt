package com.seeker.app.di

import com.seeker.app.data.network.HostnameResolver
import com.seeker.app.data.network.MdnsScanner
import com.seeker.app.data.network.NetworkRepository
import com.seeker.app.data.network.PingScanner
import com.seeker.app.data.network.PortScanner
import com.seeker.app.data.network.SnmpScanner
import com.seeker.app.data.network.NetbiosScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providePingScanner(): PingScanner = PingScanner()

    @Provides
    @Singleton
    fun providePortScanner(): PortScanner = PortScanner()

    @Provides
    @Singleton
    fun provideMdnsScanner(): MdnsScanner = MdnsScanner()

    @Provides
    @Singleton
    fun provideHostnameResolver(): HostnameResolver = HostnameResolver()

    @Provides
    @Singleton
    fun provideSnmpScanner(): SnmpScanner = SnmpScanner()

    @Provides
    @Singleton
    fun provideNetbiosScanner(): NetbiosScanner = NetbiosScanner()
}
