package com.seeker.app.ui.currentconnection;

import android.app.Application;
import com.seeker.app.core.util.LatencyMonitor;
import com.seeker.app.core.util.SpeedTest;
import com.seeker.app.data.telephony.MobileNetworkMonitor;
import com.seeker.app.data.wifi.WifiRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
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
public final class CurrentConnectionViewModel_Factory implements Factory<CurrentConnectionViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<WifiRepository> wifiRepositoryProvider;

  private final Provider<LatencyMonitor> latencyMonitorProvider;

  private final Provider<SpeedTest> speedTestProvider;

  private final Provider<MobileNetworkMonitor> mobileNetworkMonitorProvider;

  public CurrentConnectionViewModel_Factory(Provider<Application> applicationProvider,
      Provider<WifiRepository> wifiRepositoryProvider,
      Provider<LatencyMonitor> latencyMonitorProvider, Provider<SpeedTest> speedTestProvider,
      Provider<MobileNetworkMonitor> mobileNetworkMonitorProvider) {
    this.applicationProvider = applicationProvider;
    this.wifiRepositoryProvider = wifiRepositoryProvider;
    this.latencyMonitorProvider = latencyMonitorProvider;
    this.speedTestProvider = speedTestProvider;
    this.mobileNetworkMonitorProvider = mobileNetworkMonitorProvider;
  }

  @Override
  public CurrentConnectionViewModel get() {
    return newInstance(applicationProvider.get(), wifiRepositoryProvider.get(), latencyMonitorProvider.get(), speedTestProvider.get(), mobileNetworkMonitorProvider.get());
  }

  public static CurrentConnectionViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<WifiRepository> wifiRepositoryProvider,
      Provider<LatencyMonitor> latencyMonitorProvider, Provider<SpeedTest> speedTestProvider,
      Provider<MobileNetworkMonitor> mobileNetworkMonitorProvider) {
    return new CurrentConnectionViewModel_Factory(applicationProvider, wifiRepositoryProvider, latencyMonitorProvider, speedTestProvider, mobileNetworkMonitorProvider);
  }

  public static CurrentConnectionViewModel newInstance(Application application,
      WifiRepository wifiRepository, LatencyMonitor latencyMonitor, SpeedTest speedTest,
      MobileNetworkMonitor mobileNetworkMonitor) {
    return new CurrentConnectionViewModel(application, wifiRepository, latencyMonitor, speedTest, mobileNetworkMonitor);
  }
}
