package com.seeker.app.data.wifi;

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
public final class WifiScanner_Factory implements Factory<WifiScanner> {
  private final Provider<Context> contextProvider;

  public WifiScanner_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public WifiScanner get() {
    return newInstance(contextProvider.get());
  }

  public static WifiScanner_Factory create(Provider<Context> contextProvider) {
    return new WifiScanner_Factory(contextProvider);
  }

  public static WifiScanner newInstance(Context context) {
    return new WifiScanner(context);
  }
}
