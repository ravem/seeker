package com.seeker.app.data.network;

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
public final class PingScanner_Factory implements Factory<PingScanner> {
  @Override
  public PingScanner get() {
    return newInstance();
  }

  public static PingScanner_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PingScanner newInstance() {
    return new PingScanner();
  }

  private static final class InstanceHolder {
    private static final PingScanner_Factory INSTANCE = new PingScanner_Factory();
  }
}
