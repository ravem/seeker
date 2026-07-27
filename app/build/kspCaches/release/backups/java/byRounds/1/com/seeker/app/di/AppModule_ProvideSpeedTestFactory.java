package com.seeker.app.di;

import com.seeker.app.core.util.SpeedTest;
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
public final class AppModule_ProvideSpeedTestFactory implements Factory<SpeedTest> {
  @Override
  public SpeedTest get() {
    return provideSpeedTest();
  }

  public static AppModule_ProvideSpeedTestFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SpeedTest provideSpeedTest() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSpeedTest());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideSpeedTestFactory INSTANCE = new AppModule_ProvideSpeedTestFactory();
  }
}
