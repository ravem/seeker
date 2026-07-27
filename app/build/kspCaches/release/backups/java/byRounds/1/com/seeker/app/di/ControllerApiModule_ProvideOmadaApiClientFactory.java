package com.seeker.app.di;

import com.seeker.app.data.omada.OmadaApiClient;
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
public final class ControllerApiModule_ProvideOmadaApiClientFactory implements Factory<OmadaApiClient> {
  @Override
  public OmadaApiClient get() {
    return provideOmadaApiClient();
  }

  public static ControllerApiModule_ProvideOmadaApiClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OmadaApiClient provideOmadaApiClient() {
    return Preconditions.checkNotNullFromProvides(ControllerApiModule.INSTANCE.provideOmadaApiClient());
  }

  private static final class InstanceHolder {
    private static final ControllerApiModule_ProvideOmadaApiClientFactory INSTANCE = new ControllerApiModule_ProvideOmadaApiClientFactory();
  }
}
