package com.seeker.app.di;

import com.seeker.app.core.util.OuiDatabase;
import com.seeker.app.data.oui.OuiRepository;
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
public final class RepositoryModule_ProvideOuiRepositoryFactory implements Factory<OuiRepository> {
  private final Provider<OuiDatabase> ouiDatabaseProvider;

  public RepositoryModule_ProvideOuiRepositoryFactory(Provider<OuiDatabase> ouiDatabaseProvider) {
    this.ouiDatabaseProvider = ouiDatabaseProvider;
  }

  @Override
  public OuiRepository get() {
    return provideOuiRepository(ouiDatabaseProvider.get());
  }

  public static RepositoryModule_ProvideOuiRepositoryFactory create(
      Provider<OuiDatabase> ouiDatabaseProvider) {
    return new RepositoryModule_ProvideOuiRepositoryFactory(ouiDatabaseProvider);
  }

  public static OuiRepository provideOuiRepository(OuiDatabase ouiDatabase) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideOuiRepository(ouiDatabase));
  }
}
