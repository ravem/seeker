package com.seeker.app.data.telephony;

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
public final class MobileNetworkMonitor_Factory implements Factory<MobileNetworkMonitor> {
  private final Provider<Context> contextProvider;

  public MobileNetworkMonitor_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MobileNetworkMonitor get() {
    return newInstance(contextProvider.get());
  }

  public static MobileNetworkMonitor_Factory create(Provider<Context> contextProvider) {
    return new MobileNetworkMonitor_Factory(contextProvider);
  }

  public static MobileNetworkMonitor newInstance(Context context) {
    return new MobileNetworkMonitor(context);
  }
}
