package com.seeker.app;

import com.seeker.app.data.oui.OuiRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SeekerApp_MembersInjector implements MembersInjector<SeekerApp> {
  private final Provider<OuiRepository> ouiRepositoryProvider;

  public SeekerApp_MembersInjector(Provider<OuiRepository> ouiRepositoryProvider) {
    this.ouiRepositoryProvider = ouiRepositoryProvider;
  }

  public static MembersInjector<SeekerApp> create(Provider<OuiRepository> ouiRepositoryProvider) {
    return new SeekerApp_MembersInjector(ouiRepositoryProvider);
  }

  @Override
  public void injectMembers(SeekerApp instance) {
    injectOuiRepository(instance, ouiRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.seeker.app.SeekerApp.ouiRepository")
  public static void injectOuiRepository(SeekerApp instance, OuiRepository ouiRepository) {
    instance.ouiRepository = ouiRepository;
  }
}
