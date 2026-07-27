package com.seeker.app.di;

import com.seeker.app.data.network.PingScanner;
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
public final class NetworkModule_ProvidePingScannerFactory implements Factory<PingScanner> {
  @Override
  public PingScanner get() {
    return providePingScanner();
  }

  public static NetworkModule_ProvidePingScannerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PingScanner providePingScanner() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.providePingScanner());
  }

  private static final class InstanceHolder {
    private static final NetworkModule_ProvidePingScannerFactory INSTANCE = new NetworkModule_ProvidePingScannerFactory();
  }
}
