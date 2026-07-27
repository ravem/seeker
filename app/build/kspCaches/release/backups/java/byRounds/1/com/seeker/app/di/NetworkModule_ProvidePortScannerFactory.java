package com.seeker.app.di;

import com.seeker.app.data.network.PortScanner;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class NetworkModule_ProvidePortScannerFactory implements Factory<PortScanner> {
  @Override
  public PortScanner get() {
    return providePortScanner();
  }

  public static NetworkModule_ProvidePortScannerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PortScanner providePortScanner() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.providePortScanner());
  }

  private static final class InstanceHolder {
    private static final NetworkModule_ProvidePortScannerFactory INSTANCE = new NetworkModule_ProvidePortScannerFactory();
  }
}
