package com.seeker.app.di;

import com.seeker.app.data.meraki.MerakiApiClient;
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
public final class ControllerApiModule_ProvideMerakiApiClientFactory implements Factory<MerakiApiClient> {
  @Override
  public MerakiApiClient get() {
    return provideMerakiApiClient();
  }

  public static ControllerApiModule_ProvideMerakiApiClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MerakiApiClient provideMerakiApiClient() {
    return Preconditions.checkNotNullFromProvides(ControllerApiModule.INSTANCE.provideMerakiApiClient());
  }

  private static final class InstanceHolder {
    private static final ControllerApiModule_ProvideMerakiApiClientFactory INSTANCE = new ControllerApiModule_ProvideMerakiApiClientFactory();
  }
}
