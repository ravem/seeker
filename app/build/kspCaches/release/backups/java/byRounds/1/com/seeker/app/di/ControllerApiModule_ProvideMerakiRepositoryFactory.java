package com.seeker.app.di;

import com.seeker.app.data.meraki.MerakiApiClient;
import com.seeker.app.data.meraki.MerakiRepository;
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
public final class ControllerApiModule_ProvideMerakiRepositoryFactory implements Factory<MerakiRepository> {
  private final Provider<MerakiApiClient> apiClientProvider;

  public ControllerApiModule_ProvideMerakiRepositoryFactory(
      Provider<MerakiApiClient> apiClientProvider) {
    this.apiClientProvider = apiClientProvider;
  }

  @Override
  public MerakiRepository get() {
    return provideMerakiRepository(apiClientProvider.get());
  }

  public static ControllerApiModule_ProvideMerakiRepositoryFactory create(
      Provider<MerakiApiClient> apiClientProvider) {
    return new ControllerApiModule_ProvideMerakiRepositoryFactory(apiClientProvider);
  }

  public static MerakiRepository provideMerakiRepository(MerakiApiClient apiClient) {
    return Preconditions.checkNotNullFromProvides(ControllerApiModule.INSTANCE.provideMerakiRepository(apiClient));
  }
}
