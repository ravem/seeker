package com.seeker.app.ui.scanner;

import android.content.Context;
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
public final class WifiScannerViewModel_Factory implements Factory<WifiScannerViewModel> {
  private final Provider<WifiRepository> wifiRepositoryProvider;

  private final Provider<Context> contextProvider;

  public WifiScannerViewModel_Factory(Provider<WifiRepository> wifiRepositoryProvider,
      Provider<Context> contextProvider) {
    this.wifiRepositoryProvider = wifiRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public WifiScannerViewModel get() {
    return newInstance(wifiRepositoryProvider.get(), contextProvider.get());
  }

  public static WifiScannerViewModel_Factory create(Provider<WifiRepository> wifiRepositoryProvider,
      Provider<Context> contextProvider) {
    return new WifiScannerViewModel_Factory(wifiRepositoryProvider, contextProvider);
  }

  public static WifiScannerViewModel newInstance(WifiRepository wifiRepository, Context context) {
    return new WifiScannerViewModel(wifiRepository, context);
  }
}
