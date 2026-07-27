package com.seeker.app.core.util;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class OuiDatabase_Factory implements Factory<OuiDatabase> {
  private final Provider<Context> contextProvider;

  public OuiDatabase_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public OuiDatabase get() {
    return newInstance(contextProvider.get());
  }

  public static OuiDatabase_Factory create(Provider<Context> contextProvider) {
    return new OuiDatabase_Factory(contextProvider);
  }

  public static OuiDatabase newInstance(Context context) {
    return new OuiDatabase(context);
  }
}
