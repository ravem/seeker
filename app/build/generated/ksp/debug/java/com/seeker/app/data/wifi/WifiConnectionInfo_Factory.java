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
public final class WifiConnectionInfo_Factory implements Factory<WifiConnectionInfo> {
  private final Provider<Context> contextProvider;

  public WifiConnectionInfo_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public WifiConnectionInfo get() {
    return newInstance(contextProvider.get());
  }

  public static WifiConnectionInfo_Factory create(Provider<Context> contextProvider) {
    return new WifiConnectionInfo_Factory(contextProvider);
  }

  public static WifiConnectionInfo newInstance(Context context) {
    return new WifiConnectionInfo(context);
  }
}
