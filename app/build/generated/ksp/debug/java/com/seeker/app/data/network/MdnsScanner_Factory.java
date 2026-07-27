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
public final class MdnsScanner_Factory implements Factory<MdnsScanner> {
  @Override
  public MdnsScanner get() {
    return newInstance();
  }

  public static MdnsScanner_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MdnsScanner newInstance() {
    return new MdnsScanner();
  }

  private static final class InstanceHolder {
    private static final MdnsScanner_Factory INSTANCE = new MdnsScanner_Factory();
  }
}
