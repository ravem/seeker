package com.seeker.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.seeker.app.core.util.LatencyMonitor;
import com.seeker.app.core.util.OuiDatabase;
import com.seeker.app.core.util.SpeedTest;
import com.seeker.app.data.network.HostnameResolver;
import com.seeker.app.data.network.MdnsScanner;
import com.seeker.app.data.network.NetbiosScanner;
import com.seeker.app.data.network.NetworkRepository;
import com.seeker.app.data.network.PingScanner;
import com.seeker.app.data.network.PortScanner;
import com.seeker.app.data.network.SnmpScanner;
import com.seeker.app.data.oui.OuiRepository;
import com.seeker.app.data.settings.SecurePreferences;
import com.seeker.app.data.settings.UserPreferences;
import com.seeker.app.data.telephony.MobileNetworkMonitor;
import com.seeker.app.data.wifi.WifiConnectionInfo;
import com.seeker.app.data.wifi.WifiRepository;
import com.seeker.app.data.wifi.WifiScanner;
import com.seeker.app.di.AppModule_ProvideLatencyMonitorFactory;
import com.seeker.app.di.AppModule_ProvideMobileNetworkMonitorFactory;
import com.seeker.app.di.AppModule_ProvideOuiDatabaseFactory;
import com.seeker.app.di.AppModule_ProvideSpeedTestFactory;
import com.seeker.app.di.NetworkModule_ProvideHostnameResolverFactory;
import com.seeker.app.di.NetworkModule_ProvideMdnsScannerFactory;
import com.seeker.app.di.NetworkModule_ProvideNetbiosScannerFactory;
import com.seeker.app.di.NetworkModule_ProvidePingScannerFactory;
import com.seeker.app.di.NetworkModule_ProvidePortScannerFactory;
import com.seeker.app.di.NetworkModule_ProvideSnmpScannerFactory;
import com.seeker.app.di.RepositoryModule_ProvideOuiRepositoryFactory;
import com.seeker.app.di.SettingsModule_ProvideSecurePreferencesFactory;
import com.seeker.app.di.SettingsModule_ProvideUserPreferencesFactory;
import com.seeker.app.ui.controller.ControllerDashboardViewModel;
import com.seeker.app.ui.controller.ControllerDashboardViewModel_HiltModules;
import com.seeker.app.ui.currentconnection.CurrentConnectionViewModel;
import com.seeker.app.ui.currentconnection.CurrentConnectionViewModel_HiltModules;
import com.seeker.app.ui.discovery.LanDiscoveryViewModel;
import com.seeker.app.ui.discovery.LanDiscoveryViewModel_HiltModules;
import com.seeker.app.ui.integrations.IntegrationsViewModel;
import com.seeker.app.ui.integrations.IntegrationsViewModel_HiltModules;
import com.seeker.app.ui.scanner.WifiScannerViewModel;
import com.seeker.app.ui.scanner.WifiScannerViewModel_HiltModules;
import com.seeker.app.ui.settings.SettingsViewModel;
import com.seeker.app.ui.settings.SettingsViewModel_HiltModules;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideApplicationFactory;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerSeekerApp_HiltComponents_SingletonC {
  private DaggerSeekerApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public SeekerApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements SeekerApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public SeekerApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements SeekerApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public SeekerApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements SeekerApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public SeekerApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements SeekerApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public SeekerApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements SeekerApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public SeekerApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements SeekerApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public SeekerApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements SeekerApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public SeekerApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends SeekerApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends SeekerApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends SeekerApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends SeekerApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
      injectMainActivity2(arg0);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(6).put(LazyClassKeyProvider.com_seeker_app_ui_controller_ControllerDashboardViewModel, ControllerDashboardViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_seeker_app_ui_currentconnection_CurrentConnectionViewModel, CurrentConnectionViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_seeker_app_ui_integrations_IntegrationsViewModel, IntegrationsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_seeker_app_ui_discovery_LanDiscoveryViewModel, LanDiscoveryViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_seeker_app_ui_settings_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_seeker_app_ui_scanner_WifiScannerViewModel, WifiScannerViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectUserPreferences(instance, singletonCImpl.provideUserPreferencesProvider.get());
      return instance;
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_seeker_app_ui_settings_SettingsViewModel = "com.seeker.app.ui.settings.SettingsViewModel";

      static String com_seeker_app_ui_currentconnection_CurrentConnectionViewModel = "com.seeker.app.ui.currentconnection.CurrentConnectionViewModel";

      static String com_seeker_app_ui_integrations_IntegrationsViewModel = "com.seeker.app.ui.integrations.IntegrationsViewModel";

      static String com_seeker_app_ui_discovery_LanDiscoveryViewModel = "com.seeker.app.ui.discovery.LanDiscoveryViewModel";

      static String com_seeker_app_ui_scanner_WifiScannerViewModel = "com.seeker.app.ui.scanner.WifiScannerViewModel";

      static String com_seeker_app_ui_controller_ControllerDashboardViewModel = "com.seeker.app.ui.controller.ControllerDashboardViewModel";

      @KeepFieldType
      SettingsViewModel com_seeker_app_ui_settings_SettingsViewModel2;

      @KeepFieldType
      CurrentConnectionViewModel com_seeker_app_ui_currentconnection_CurrentConnectionViewModel2;

      @KeepFieldType
      IntegrationsViewModel com_seeker_app_ui_integrations_IntegrationsViewModel2;

      @KeepFieldType
      LanDiscoveryViewModel com_seeker_app_ui_discovery_LanDiscoveryViewModel2;

      @KeepFieldType
      WifiScannerViewModel com_seeker_app_ui_scanner_WifiScannerViewModel2;

      @KeepFieldType
      ControllerDashboardViewModel com_seeker_app_ui_controller_ControllerDashboardViewModel2;
    }
  }

  private static final class ViewModelCImpl extends SeekerApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<ControllerDashboardViewModel> controllerDashboardViewModelProvider;

    private Provider<CurrentConnectionViewModel> currentConnectionViewModelProvider;

    private Provider<IntegrationsViewModel> integrationsViewModelProvider;

    private Provider<LanDiscoveryViewModel> lanDiscoveryViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<WifiScannerViewModel> wifiScannerViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.controllerDashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.currentConnectionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.integrationsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.lanDiscoveryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.wifiScannerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(6).put(LazyClassKeyProvider.com_seeker_app_ui_controller_ControllerDashboardViewModel, ((Provider) controllerDashboardViewModelProvider)).put(LazyClassKeyProvider.com_seeker_app_ui_currentconnection_CurrentConnectionViewModel, ((Provider) currentConnectionViewModelProvider)).put(LazyClassKeyProvider.com_seeker_app_ui_integrations_IntegrationsViewModel, ((Provider) integrationsViewModelProvider)).put(LazyClassKeyProvider.com_seeker_app_ui_discovery_LanDiscoveryViewModel, ((Provider) lanDiscoveryViewModelProvider)).put(LazyClassKeyProvider.com_seeker_app_ui_settings_SettingsViewModel, ((Provider) settingsViewModelProvider)).put(LazyClassKeyProvider.com_seeker_app_ui_scanner_WifiScannerViewModel, ((Provider) wifiScannerViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_seeker_app_ui_controller_ControllerDashboardViewModel = "com.seeker.app.ui.controller.ControllerDashboardViewModel";

      static String com_seeker_app_ui_discovery_LanDiscoveryViewModel = "com.seeker.app.ui.discovery.LanDiscoveryViewModel";

      static String com_seeker_app_ui_settings_SettingsViewModel = "com.seeker.app.ui.settings.SettingsViewModel";

      static String com_seeker_app_ui_integrations_IntegrationsViewModel = "com.seeker.app.ui.integrations.IntegrationsViewModel";

      static String com_seeker_app_ui_scanner_WifiScannerViewModel = "com.seeker.app.ui.scanner.WifiScannerViewModel";

      static String com_seeker_app_ui_currentconnection_CurrentConnectionViewModel = "com.seeker.app.ui.currentconnection.CurrentConnectionViewModel";

      @KeepFieldType
      ControllerDashboardViewModel com_seeker_app_ui_controller_ControllerDashboardViewModel2;

      @KeepFieldType
      LanDiscoveryViewModel com_seeker_app_ui_discovery_LanDiscoveryViewModel2;

      @KeepFieldType
      SettingsViewModel com_seeker_app_ui_settings_SettingsViewModel2;

      @KeepFieldType
      IntegrationsViewModel com_seeker_app_ui_integrations_IntegrationsViewModel2;

      @KeepFieldType
      WifiScannerViewModel com_seeker_app_ui_scanner_WifiScannerViewModel2;

      @KeepFieldType
      CurrentConnectionViewModel com_seeker_app_ui_currentconnection_CurrentConnectionViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.seeker.app.ui.controller.ControllerDashboardViewModel 
          return (T) new ControllerDashboardViewModel(singletonCImpl.provideSecurePreferencesProvider.get());

          case 1: // com.seeker.app.ui.currentconnection.CurrentConnectionViewModel 
          return (T) new CurrentConnectionViewModel(ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule), singletonCImpl.wifiRepositoryProvider.get(), singletonCImpl.provideLatencyMonitorProvider.get(), singletonCImpl.provideSpeedTestProvider.get(), singletonCImpl.provideMobileNetworkMonitorProvider.get());

          case 2: // com.seeker.app.ui.integrations.IntegrationsViewModel 
          return (T) new IntegrationsViewModel(singletonCImpl.provideSecurePreferencesProvider.get());

          case 3: // com.seeker.app.ui.discovery.LanDiscoveryViewModel 
          return (T) new LanDiscoveryViewModel(singletonCImpl.networkRepositoryProvider.get(), singletonCImpl.wifiRepositoryProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.seeker.app.ui.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.provideUserPreferencesProvider.get());

          case 5: // com.seeker.app.ui.scanner.WifiScannerViewModel 
          return (T) new WifiScannerViewModel(singletonCImpl.wifiRepositoryProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends SeekerApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends SeekerApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends SeekerApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<OuiDatabase> provideOuiDatabaseProvider;

    private Provider<OuiRepository> provideOuiRepositoryProvider;

    private Provider<UserPreferences> provideUserPreferencesProvider;

    private Provider<SecurePreferences> provideSecurePreferencesProvider;

    private Provider<WifiScanner> wifiScannerProvider;

    private Provider<WifiConnectionInfo> wifiConnectionInfoProvider;

    private Provider<WifiRepository> wifiRepositoryProvider;

    private Provider<LatencyMonitor> provideLatencyMonitorProvider;

    private Provider<SpeedTest> provideSpeedTestProvider;

    private Provider<MobileNetworkMonitor> provideMobileNetworkMonitorProvider;

    private Provider<PingScanner> providePingScannerProvider;

    private Provider<PortScanner> providePortScannerProvider;

    private Provider<MdnsScanner> provideMdnsScannerProvider;

    private Provider<HostnameResolver> provideHostnameResolverProvider;

    private Provider<SnmpScanner> provideSnmpScannerProvider;

    private Provider<NetbiosScanner> provideNetbiosScannerProvider;

    private Provider<NetworkRepository> networkRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideOuiDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<OuiDatabase>(singletonCImpl, 1));
      this.provideOuiRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<OuiRepository>(singletonCImpl, 0));
      this.provideUserPreferencesProvider = DoubleCheck.provider(new SwitchingProvider<UserPreferences>(singletonCImpl, 2));
      this.provideSecurePreferencesProvider = DoubleCheck.provider(new SwitchingProvider<SecurePreferences>(singletonCImpl, 3));
      this.wifiScannerProvider = DoubleCheck.provider(new SwitchingProvider<WifiScanner>(singletonCImpl, 5));
      this.wifiConnectionInfoProvider = DoubleCheck.provider(new SwitchingProvider<WifiConnectionInfo>(singletonCImpl, 6));
      this.wifiRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<WifiRepository>(singletonCImpl, 4));
      this.provideLatencyMonitorProvider = DoubleCheck.provider(new SwitchingProvider<LatencyMonitor>(singletonCImpl, 7));
      this.provideSpeedTestProvider = DoubleCheck.provider(new SwitchingProvider<SpeedTest>(singletonCImpl, 8));
      this.provideMobileNetworkMonitorProvider = DoubleCheck.provider(new SwitchingProvider<MobileNetworkMonitor>(singletonCImpl, 9));
      this.providePingScannerProvider = DoubleCheck.provider(new SwitchingProvider<PingScanner>(singletonCImpl, 11));
      this.providePortScannerProvider = DoubleCheck.provider(new SwitchingProvider<PortScanner>(singletonCImpl, 12));
      this.provideMdnsScannerProvider = DoubleCheck.provider(new SwitchingProvider<MdnsScanner>(singletonCImpl, 13));
      this.provideHostnameResolverProvider = DoubleCheck.provider(new SwitchingProvider<HostnameResolver>(singletonCImpl, 14));
      this.provideSnmpScannerProvider = DoubleCheck.provider(new SwitchingProvider<SnmpScanner>(singletonCImpl, 15));
      this.provideNetbiosScannerProvider = DoubleCheck.provider(new SwitchingProvider<NetbiosScanner>(singletonCImpl, 16));
      this.networkRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<NetworkRepository>(singletonCImpl, 10));
    }

    @Override
    public void injectSeekerApp(SeekerApp arg0) {
      injectSeekerApp2(arg0);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private SeekerApp injectSeekerApp2(SeekerApp instance) {
      SeekerApp_MembersInjector.injectOuiRepository(instance, provideOuiRepositoryProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.seeker.app.data.oui.OuiRepository 
          return (T) RepositoryModule_ProvideOuiRepositoryFactory.provideOuiRepository(singletonCImpl.provideOuiDatabaseProvider.get());

          case 1: // com.seeker.app.core.util.OuiDatabase 
          return (T) AppModule_ProvideOuiDatabaseFactory.provideOuiDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.seeker.app.data.settings.UserPreferences 
          return (T) SettingsModule_ProvideUserPreferencesFactory.provideUserPreferences(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.seeker.app.data.settings.SecurePreferences 
          return (T) SettingsModule_ProvideSecurePreferencesFactory.provideSecurePreferences(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.seeker.app.data.wifi.WifiRepository 
          return (T) new WifiRepository(singletonCImpl.wifiScannerProvider.get(), singletonCImpl.wifiConnectionInfoProvider.get(), singletonCImpl.provideOuiDatabaseProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // com.seeker.app.data.wifi.WifiScanner 
          return (T) new WifiScanner(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.seeker.app.data.wifi.WifiConnectionInfo 
          return (T) new WifiConnectionInfo(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 7: // com.seeker.app.core.util.LatencyMonitor 
          return (T) AppModule_ProvideLatencyMonitorFactory.provideLatencyMonitor();

          case 8: // com.seeker.app.core.util.SpeedTest 
          return (T) AppModule_ProvideSpeedTestFactory.provideSpeedTest();

          case 9: // com.seeker.app.data.telephony.MobileNetworkMonitor 
          return (T) AppModule_ProvideMobileNetworkMonitorFactory.provideMobileNetworkMonitor(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 10: // com.seeker.app.data.network.NetworkRepository 
          return (T) new NetworkRepository(singletonCImpl.providePingScannerProvider.get(), singletonCImpl.providePortScannerProvider.get(), singletonCImpl.provideMdnsScannerProvider.get(), singletonCImpl.provideHostnameResolverProvider.get(), singletonCImpl.provideSnmpScannerProvider.get(), singletonCImpl.provideNetbiosScannerProvider.get());

          case 11: // com.seeker.app.data.network.PingScanner 
          return (T) NetworkModule_ProvidePingScannerFactory.providePingScanner();

          case 12: // com.seeker.app.data.network.PortScanner 
          return (T) NetworkModule_ProvidePortScannerFactory.providePortScanner();

          case 13: // com.seeker.app.data.network.MdnsScanner 
          return (T) NetworkModule_ProvideMdnsScannerFactory.provideMdnsScanner();

          case 14: // com.seeker.app.data.network.HostnameResolver 
          return (T) NetworkModule_ProvideHostnameResolverFactory.provideHostnameResolver();

          case 15: // com.seeker.app.data.network.SnmpScanner 
          return (T) NetworkModule_ProvideSnmpScannerFactory.provideSnmpScanner();

          case 16: // com.seeker.app.data.network.NetbiosScanner 
          return (T) NetworkModule_ProvideNetbiosScannerFactory.provideNetbiosScanner();

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
