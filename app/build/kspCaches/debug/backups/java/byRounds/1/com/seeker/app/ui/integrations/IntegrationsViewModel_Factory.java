package com.seeker.app.ui.integrations;

import com.seeker.app.data.settings.SecurePreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class IntegrationsViewModel_Factory implements Factory<IntegrationsViewModel> {
  private final Provider<SecurePreferences> securePreferencesProvider;

  public IntegrationsViewModel_Factory(Provider<SecurePreferences> securePreferencesProvider) {
    this.securePreferencesProvider = securePreferencesProvider;
  }

  @Override
  public IntegrationsViewModel get() {
    return newInstance(securePreferencesProvider.get());
  }

  public static IntegrationsViewModel_Factory create(
      Provider<SecurePreferences> securePreferencesProvider) {
    return new IntegrationsViewModel_Factory(securePreferencesProvider);
  }

  public static IntegrationsViewModel newInstance(SecurePreferences securePreferences) {
    return new IntegrationsViewModel(securePreferences);
  }
}
