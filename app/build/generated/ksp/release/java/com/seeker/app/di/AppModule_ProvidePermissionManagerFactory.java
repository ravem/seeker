package com.seeker.app.di;

import android.content.Context;
import com.seeker.app.core.permissions.PermissionManager;
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
public final class AppModule_ProvidePermissionManagerFactory implements Factory<PermissionManager> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvidePermissionManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PermissionManager get() {
    return providePermissionManager(contextProvider.get());
  }

  public static AppModule_ProvidePermissionManagerFactory create(
      Provider<Context> contextProvider) {
    return new AppModule_ProvidePermissionManagerFactory(contextProvider);
  }

  public static PermissionManager providePermissionManager(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePermissionManager(context));
  }
}
