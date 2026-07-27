package com.seeker.app.data.wifi;

import android.content.Context;
import com.seeker.app.core.util.OuiDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class WifiRepository_Factory implements Factory<WifiRepository> {
  private final Provider<WifiScanner> wifiScannerProvider;

  private final Provider<WifiConnectionInfo> wifiConnectionInfoProvider;

  private final Provider<OuiDatabase> ouiDatabaseProvider;

  private final Provider<Context> contextProvider;

  public WifiRepository_Factory(Provider<WifiScanner> wifiScannerProvider,
      Provider<WifiConnectionInfo> wifiConnectionInfoProvider,
      Provider<OuiDatabase> ouiDatabaseProvider, Provider<Context> contextProvider) {
    this.wifiScannerProvider = wifiScannerProvider;
    this.wifiConnectionInfoProvider = wifiConnectionInfoProvider;
    this.ouiDatabaseProvider = ouiDatabaseProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public WifiRepository get() {
    return newInstance(wifiScannerProvider.get(), wifiConnectionInfoProvider.get(), ouiDatabaseProvider.get(), contextProvider.get());
  }

  public static WifiRepository_Factory create(Provider<WifiScanner> wifiScannerProvider,
      Provider<WifiConnectionInfo> wifiConnectionInfoProvider,
      Provider<OuiDatabase> ouiDatabaseProvider, Provider<Context> contextProvider) {
    return new WifiRepository_Factory(wifiScannerProvider, wifiConnectionInfoProvider, ouiDatabaseProvider, contextProvider);
  }

  public static WifiRepository newInstance(WifiScanner wifiScanner,
      WifiConnectionInfo wifiConnectionInfo, OuiDatabase ouiDatabase, Context context) {
    return new WifiRepository(wifiScanner, wifiConnectionInfo, ouiDatabase, context);
  }
}
