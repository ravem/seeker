package com.seeker.app.data.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class NetworkRepository_Factory implements Factory<NetworkRepository> {
  private final Provider<PingScanner> pingScannerProvider;

  private final Provider<PortScanner> portScannerProvider;

  private final Provider<MdnsScanner> mdnsScannerProvider;

  private final Provider<HostnameResolver> hostnameResolverProvider;

  private final Provider<SnmpScanner> snmpScannerProvider;

  private final Provider<NetbiosScanner> netbiosScannerProvider;

  public NetworkRepository_Factory(Provider<PingScanner> pingScannerProvider,
      Provider<PortScanner> portScannerProvider, Provider<MdnsScanner> mdnsScannerProvider,
      Provider<HostnameResolver> hostnameResolverProvider,
      Provider<SnmpScanner> snmpScannerProvider, Provider<NetbiosScanner> netbiosScannerProvider) {
    this.pingScannerProvider = pingScannerProvider;
    this.portScannerProvider = portScannerProvider;
    this.mdnsScannerProvider = mdnsScannerProvider;
    this.hostnameResolverProvider = hostnameResolverProvider;
    this.snmpScannerProvider = snmpScannerProvider;
    this.netbiosScannerProvider = netbiosScannerProvider;
  }

  @Override
  public NetworkRepository get() {
    return newInstance(pingScannerProvider.get(), portScannerProvider.get(), mdnsScannerProvider.get(), hostnameResolverProvider.get(), snmpScannerProvider.get(), netbiosScannerProvider.get());
  }

  public static NetworkRepository_Factory create(Provider<PingScanner> pingScannerProvider,
      Provider<PortScanner> portScannerProvider, Provider<MdnsScanner> mdnsScannerProvider,
      Provider<HostnameResolver> hostnameResolverProvider,
      Provider<SnmpScanner> snmpScannerProvider, Provider<NetbiosScanner> netbiosScannerProvider) {
    return new NetworkRepository_Factory(pingScannerProvider, portScannerProvider, mdnsScannerProvider, hostnameResolverProvider, snmpScannerProvider, netbiosScannerProvider);
  }

  public static NetworkRepository newInstance(PingScanner pingScanner, PortScanner portScanner,
      MdnsScanner mdnsScanner, HostnameResolver hostnameResolver, SnmpScanner snmpScanner,
      NetbiosScanner netbiosScanner) {
    return new NetworkRepository(pingScanner, portScanner, mdnsScanner, hostnameResolver, snmpScanner, netbiosScanner);
  }
}
