package com.seeker.app.di;

import com.seeker.app.data.network.MdnsScanner;
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
public final class NetworkModule_ProvideMdnsScannerFactory implements Factory<MdnsScanner> {
  @Override
  public MdnsScanner get() {
    return provideMdnsScanner();
  }

  public static NetworkModule_ProvideMdnsScannerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MdnsScanner provideMdnsScanner() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideMdnsScanner());
  }

  private static final class InstanceHolder {
    private static final NetworkModule_ProvideMdnsScannerFactory INSTANCE = new NetworkModule_ProvideMdnsScannerFactory();
  }
}
