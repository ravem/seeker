package com.seeker.app.ui.discovery;

import android.content.Context;
import com.seeker.app.data.network.NetworkRepository;
import com.seeker.app.data.wifi.WifiRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class LanDiscoveryViewModel_Factory implements Factory<LanDiscoveryViewModel> {
  private final Provider<NetworkRepository> networkRepositoryProvider;

  private final Provider<WifiRepository> wifiRepositoryProvider;

  private final Provider<Context> contextProvider;

  public LanDiscoveryViewModel_Factory(Provider<NetworkRepository> networkRepositoryProvider,
      Provider<WifiRepository> wifiRepositoryProvider, Provider<Context> contextProvider) {
    this.networkRepositoryProvider = networkRepositoryProvider;
    this.wifiRepositoryProvider = wifiRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public LanDiscoveryViewModel get() {
    return newInstance(networkRepositoryProvider.get(), wifiRepositoryProvider.get(), contextProvider.get());
  }

  public static LanDiscoveryViewModel_Factory create(
      Provider<NetworkRepository> networkRepositoryProvider,
      Provider<WifiRepository> wifiRepositoryProvider, Provider<Context> contextProvider) {
    return new LanDiscoveryViewModel_Factory(networkRepositoryProvider, wifiRepositoryProvider, contextProvider);
  }

  public static LanDiscoveryViewModel newInstance(NetworkRepository networkRepository,
      WifiRepository wifiRepository, Context context) {
    return new LanDiscoveryViewModel(networkRepository, wifiRepository, context);
  }
}
