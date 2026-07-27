package com.seeker.app.data.oui;

import com.seeker.app.core.util.OuiDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class OuiRepository_Factory implements Factory<OuiRepository> {
  private final Provider<OuiDatabase> ouiDatabaseProvider;

  public OuiRepository_Factory(Provider<OuiDatabase> ouiDatabaseProvider) {
    this.ouiDatabaseProvider = ouiDatabaseProvider;
  }

  @Override
  public OuiRepository get() {
    return newInstance(ouiDatabaseProvider.get());
  }

  public static OuiRepository_Factory create(Provider<OuiDatabase> ouiDatabaseProvider) {
    return new OuiRepository_Factory(ouiDatabaseProvider);
  }

  public static OuiRepository newInstance(OuiDatabase ouiDatabase) {
    return new OuiRepository(ouiDatabase);
  }
}
