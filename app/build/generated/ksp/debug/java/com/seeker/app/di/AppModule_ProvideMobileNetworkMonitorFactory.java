package com.seeker.app.di;

import android.content.Context;
import com.seeker.app.data.telephony.MobileNetworkMonitor;
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
public final class AppModule_ProvideMobileNetworkMonitorFactory implements Factory<MobileNetworkMonitor> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideMobileNetworkMonitorFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MobileNetworkMonitor get() {
    return provideMobileNetworkMonitor(contextProvider.get());
  }

  public static AppModule_ProvideMobileNetworkMonitorFactory create(
      Provider<Context> contextProvider) {
    return new AppModule_ProvideMobileNetworkMonitorFactory(contextProvider);
  }

  public static MobileNetworkMonitor provideMobileNetworkMonitor(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMobileNetworkMonitor(context));
  }
}
