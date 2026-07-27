package com.seeker.app.di;

import com.seeker.app.data.network.NetbiosScanner;
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
public final class NetworkModule_ProvideNetbiosScannerFactory implements Factory<NetbiosScanner> {
  @Override
  public NetbiosScanner get() {
    return provideNetbiosScanner();
  }

  public static NetworkModule_ProvideNetbiosScannerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NetbiosScanner provideNetbiosScanner() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideNetbiosScanner());
  }

  private static final class InstanceHolder {
    private static final NetworkModule_ProvideNetbiosScannerFactory INSTANCE = new NetworkModule_ProvideNetbiosScannerFactory();
  }
}
