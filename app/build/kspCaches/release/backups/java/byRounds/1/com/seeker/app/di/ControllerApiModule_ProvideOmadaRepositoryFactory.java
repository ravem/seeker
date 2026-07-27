package com.seeker.app.di;

import com.seeker.app.data.omada.OmadaApiClient;
import com.seeker.app.data.omada.OmadaRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ControllerApiModule_ProvideOmadaRepositoryFactory implements Factory<OmadaRepository> {
  private final Provider<OmadaApiClient> apiClientProvider;

  public ControllerApiModule_ProvideOmadaRepositoryFactory(
      Provider<OmadaApiClient> apiClientProvider) {
    this.apiClientProvider = apiClientProvider;
  }

  @Override
  public OmadaRepository get() {
    return provideOmadaRepository(apiClientProvider.get());
  }

  public static ControllerApiModule_ProvideOmadaRepositoryFactory create(
      Provider<OmadaApiClient> apiClientProvider) {
    return new ControllerApiModule_ProvideOmadaRepositoryFactory(apiClientProvider);
  }

  public static OmadaRepository provideOmadaRepository(OmadaApiClient apiClient) {
    return Preconditions.checkNotNullFromProvides(ControllerApiModule.INSTANCE.provideOmadaRepository(apiClient));
  }
}
