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
public final class HostnameResolver_Factory implements Factory<HostnameResolver> {
  @Override
  public HostnameResolver get() {
    return newInstance();
  }

  public static HostnameResolver_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static HostnameResolver newInstance() {
    return new HostnameResolver();
  }

  private static final class InstanceHolder {
    private static final HostnameResolver_Factory INSTANCE = new HostnameResolver_Factory();
  }
}
