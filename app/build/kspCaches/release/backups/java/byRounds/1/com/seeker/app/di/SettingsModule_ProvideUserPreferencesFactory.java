package com.seeker.app.di;

import android.content.Context;
import com.seeker.app.data.settings.UserPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SettingsModule_ProvideUserPreferencesFactory implements Factory<UserPreferences> {
  private final Provider<Context> contextProvider;

  public SettingsModule_ProvideUserPreferencesFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public UserPreferences get() {
    return provideUserPreferences(contextProvider.get());
  }

  public static SettingsModule_ProvideUserPreferencesFactory create(
      Provider<Context> contextProvider) {
    return new SettingsModule_ProvideUserPreferencesFactory(contextProvider);
  }

  public static UserPreferences provideUserPreferences(Context context) {
    return Preconditions.checkNotNullFromProvides(SettingsModule.INSTANCE.provideUserPreferences(context));
  }
}
