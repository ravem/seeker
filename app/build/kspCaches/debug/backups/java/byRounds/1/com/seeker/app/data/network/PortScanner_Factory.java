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
public final class PortScanner_Factory implements Factory<PortScanner> {
  @Override
  public PortScanner get() {
    return newInstance();
  }

  public static PortScanner_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PortScanner newInstance() {
    return new PortScanner();
  }

  private static final class InstanceHolder {
    private static final PortScanner_Factory INSTANCE = new PortScanner_Factory();
  }
}
