package com.seeker.app.core.util;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class LatencyMonitor_Factory implements Factory<LatencyMonitor> {
  @Override
  public LatencyMonitor get() {
    return newInstance();
  }

  public static LatencyMonitor_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LatencyMonitor newInstance() {
    return new LatencyMonitor();
  }

  private static final class InstanceHolder {
    private static final LatencyMonitor_Factory INSTANCE = new LatencyMonitor_Factory();
  }
}
