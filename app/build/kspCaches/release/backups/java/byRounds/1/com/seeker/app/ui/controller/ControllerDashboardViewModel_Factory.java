package com.seeker.app.ui.controller;

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
public final class ControllerDashboardViewModel_Factory implements Factory<ControllerDashboardViewModel> {
  private final Provider<SecurePreferences> securePreferencesProvider;

  public ControllerDashboardViewModel_Factory(
      Provider<SecurePreferences> securePreferencesProvider) {
    this.securePreferencesProvider = securePreferencesProvider;
  }

  @Override
  public ControllerDashboardViewModel get() {
    return newInstance(securePreferencesProvider.get());
  }

  public static ControllerDashboardViewModel_Factory create(
      Provider<SecurePreferences> securePreferencesProvider) {
    return new ControllerDashboardViewModel_Factory(securePreferencesProvider);
  }

  public static ControllerDashboardViewModel newInstance(SecurePreferences securePreferences) {
    return new ControllerDashboardViewModel(securePreferences);
  }
}
