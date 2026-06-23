package com.orbit.licensehub.config;

import com.orbit.licensehub.entity.*;
import com.orbit.licensehub.repository.ApplicationRepository;
import com.orbit.licensehub.repository.TenantRepository;
import com.orbit.licensehub.repository.UserRepository;
import com.orbit.licensehub.service.CryptoService;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

  private final UserRepository userRepository;
  private final TenantRepository tenantRepository;
  private final ApplicationRepository applicationRepository;
  private final PasswordEncoder passwordEncoder;
  private final CryptoService cryptoService;

  public DataSeeder(
      UserRepository userRepository,
      TenantRepository tenantRepository,
      ApplicationRepository applicationRepository,
      PasswordEncoder passwordEncoder,
      CryptoService cryptoService) {
    this.userRepository = userRepository;
    this.tenantRepository = tenantRepository;
    this.applicationRepository = applicationRepository;
    this.passwordEncoder = passwordEncoder;
    this.cryptoService = cryptoService;
  }

  @Override
  public void run(String... args) {
    if (tenantRepository.count() == 0) {
      TenantEntity tenant = new TenantEntity();
      tenant.setName("Demo Tenant");
      tenant.setPurchasedLicenses(100);
      tenant.setAssignedLicenses(25);
      tenant.setStatus(TenantStatus.ACTIVE);
      tenant = tenantRepository.save(tenant);

      ApplicationEntity app = new ApplicationEntity();
      app.setTenant(tenant);
      app.setName("Demo Sign App");
      app.setLogoUrl("https://example.com/logo.png");
      app.setBaseColor("#102844");
      app.setEnvironment(ApplicationEnvironment.DEV);
      app.setP12FileRef("/secrets/demo.p12");
      app.setP12PasswordEncrypted(cryptoService.encrypt("demoP12Password"));
      app.setRetentionMinutes(90);
      app.setPublicKeyPem("-----BEGIN PUBLIC KEY-----\\nDEMO\\n-----END PUBLIC KEY-----");
      app.setAllocatedLicenses(25);
      app.setClientApiKey(randomToken(18));
      app.setClientSecretEncrypted(cryptoService.encrypt(randomToken(24)));
      app.setStatus(ApplicationStatus.ACTIVE);
      applicationRepository.save(app);
    }

    if (userRepository.count() == 0) {
      TenantEntity demoTenant = tenantRepository.findAll().get(0);

      UserEntity superAdmin = new UserEntity();
      superAdmin.setUsername("superadmin");
      superAdmin.setEmail("superadmin@orbit.local");
      superAdmin.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
      superAdmin.setRole(UserRole.SUPER_ADMIN);
      superAdmin.setEnabled(true);
      superAdmin.setExtraPermissions(Set.of());
      userRepository.save(superAdmin);

      UserEntity tenantAdmin = new UserEntity();
      tenantAdmin.setUsername("tenantadmin");
      tenantAdmin.setEmail("tenantadmin@orbit.local");
      tenantAdmin.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
      tenantAdmin.setRole(UserRole.TENANT_ADMIN);
      tenantAdmin.setTenant(demoTenant);
      tenantAdmin.setEnabled(true);
      tenantAdmin.setExtraPermissions(Set.of("REQUEST_EXTRA_LICENSES"));
      userRepository.save(tenantAdmin);

      UserEntity licenseAdmin = new UserEntity();
      licenseAdmin.setUsername("licenseadmin");
      licenseAdmin.setEmail("licenseadmin@orbit.local");
      licenseAdmin.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
      licenseAdmin.setRole(UserRole.LICENSE_ADMIN);
      licenseAdmin.setTenant(demoTenant);
      licenseAdmin.setEnabled(true);
      licenseAdmin.setExtraPermissions(Set.of());
      userRepository.save(licenseAdmin);

      UserEntity viewer = new UserEntity();
      viewer.setUsername("viewer");
      viewer.setEmail("viewer@orbit.local");
      viewer.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
      viewer.setRole(UserRole.LICENSE_VIEWER);
      viewer.setTenant(demoTenant);
      viewer.setEnabled(true);
      viewer.setExtraPermissions(Set.of());
      userRepository.save(viewer);
    }
  }

  private String randomToken(int byteSize) {
    byte[] bytes = new byte[byteSize];
    new SecureRandom().nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
