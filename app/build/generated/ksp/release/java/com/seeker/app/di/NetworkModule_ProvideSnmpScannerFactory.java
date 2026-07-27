package com.seeker.app.di;

import com.seeker.app.data.network.SnmpScanner;
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
public final class NetworkModule_ProvideSnmpScannerFactory implements Factory<SnmpScanner> {
  @Override
  public SnmpScanner get() {
    return provideSnmpScanner();
  }

  public static NetworkModule_ProvideSnmpScannerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SnmpScanner provideSnmpScanner() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideSnmpScanner());
  }

  private static final class InstanceHolder {
    private static final NetworkModule_ProvideSnmpScannerFactory INSTANCE = new NetworkModule_ProvideSnmpScannerFactory();
  }
}
