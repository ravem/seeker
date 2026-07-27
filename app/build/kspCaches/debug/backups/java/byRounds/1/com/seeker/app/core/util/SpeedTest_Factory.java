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
public final class SpeedTest_Factory implements Factory<SpeedTest> {
  @Override
  public SpeedTest get() {
    return newInstance();
  }

  public static SpeedTest_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SpeedTest newInstance() {
    return new SpeedTest();
  }

  private static final class InstanceHolder {
    private static final SpeedTest_Factory INSTANCE = new SpeedTest_Factory();
  }
}
