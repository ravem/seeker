package com.seeker.app.di;

import com.seeker.app.data.network.HostnameResolver;
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
public final class NetworkModule_ProvideHostnameResolverFactory implements Factory<HostnameResolver> {
  @Override
  public HostnameResolver get() {
    return provideHostnameResolver();
  }

  public static NetworkModule_ProvideHostnameResolverFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static HostnameResolver provideHostnameResolver() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideHostnameResolver());
  }

  private static final class InstanceHolder {
    private static final NetworkModule_ProvideHostnameResolverFactory INSTANCE = new NetworkModule_ProvideHostnameResolverFactory();
  }
}
