package com.seeker.app.di;

import com.seeker.app.data.unifi.UniFiApiClient;
import com.seeker.app.data.unifi.UniFiRepository;
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
public final class ControllerApiModule_ProvideUniFiRepositoryFactory implements Factory<UniFiRepository> {
  private final Provider<UniFiApiClient> apiClientProvider;

  public ControllerApiModule_ProvideUniFiRepositoryFactory(
      Provider<UniFiApiClient> apiClientProvider) {
    this.apiClientProvider = apiClientProvider;
  }

  @Override
  public UniFiRepository get() {
    return provideUniFiRepository(apiClientProvider.get());
  }

  public static ControllerApiModule_ProvideUniFiRepositoryFactory create(
      Provider<UniFiApiClient> apiClientProvider) {
    return new ControllerApiModule_ProvideUniFiRepositoryFactory(apiClientProvider);
  }

  public static UniFiRepository provideUniFiRepository(UniFiApiClient apiClient) {
    return Preconditions.checkNotNullFromProvides(ControllerApiModule.INSTANCE.provideUniFiRepository(apiClient));
  }
}
