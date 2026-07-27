package com.seeker.app.di;

import android.content.Context;
import com.seeker.app.core.util.OuiDatabase;
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
public final class AppModule_ProvideOuiDatabaseFactory implements Factory<OuiDatabase> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideOuiDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public OuiDatabase get() {
    return provideOuiDatabase(contextProvider.get());
  }

  public static AppModule_ProvideOuiDatabaseFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvideOuiDatabaseFactory(contextProvider);
  }

  public static OuiDatabase provideOuiDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideOuiDatabase(context));
  }
}
