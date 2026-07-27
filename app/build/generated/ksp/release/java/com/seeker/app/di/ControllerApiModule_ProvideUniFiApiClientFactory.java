package com.seeker.app.di;

import com.seeker.app.data.unifi.UniFiApiClient;
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
public final class ControllerApiModule_ProvideUniFiApiClientFactory implements Factory<UniFiApiClient> {
  @Override
  public UniFiApiClient get() {
    return provideUniFiApiClient();
  }

  public static ControllerApiModule_ProvideUniFiApiClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static UniFiApiClient provideUniFiApiClient() {
    return Preconditions.checkNotNullFromProvides(ControllerApiModule.INSTANCE.provideUniFiApiClient());
  }

  private static final class InstanceHolder {
    private static final ControllerApiModule_ProvideUniFiApiClientFactory INSTANCE = new ControllerApiModule_ProvideUniFiApiClientFactory();
  }
}
