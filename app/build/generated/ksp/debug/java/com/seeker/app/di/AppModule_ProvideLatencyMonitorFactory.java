package com.seeker.app.di;

import com.seeker.app.core.util.LatencyMonitor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class AppModule_ProvideLatencyMonitorFactory implements Factory<LatencyMonitor> {
  @Override
  public LatencyMonitor get() {
    return provideLatencyMonitor();
  }

  public static AppModule_ProvideLatencyMonitorFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LatencyMonitor provideLatencyMonitor() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideLatencyMonitor());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideLatencyMonitorFactory INSTANCE = new AppModule_ProvideLatencyMonitorFactory();
  }
}
