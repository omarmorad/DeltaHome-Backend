package com.deltahomes.backend.config;

import com.deltahomes.backend.entity.Follower;
import com.deltahomes.backend.entity.Review;
import com.deltahomes.backend.entity.SavedItem;
import com.deltahomes.backend.entity.admin.AuditLog;
import com.deltahomes.backend.entity.admin.CmsPage;
import com.deltahomes.backend.entity.admin.FeatureFlag;
import com.deltahomes.backend.entity.commerce.Coupon;
import com.deltahomes.backend.entity.commerce.Payment;
import com.deltahomes.backend.entity.commerce.Subscription;
import com.deltahomes.backend.entity.commerce.SubscriptionPlan;
import com.deltahomes.backend.entity.communication.Appointment;
import com.deltahomes.backend.entity.communication.Conversation;
import com.deltahomes.backend.entity.communication.Message;
import com.deltahomes.backend.entity.communication.Notification;
import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.entity.company.CompanyPortfolio;
import com.deltahomes.backend.entity.company.CompanyService;
import com.deltahomes.backend.entity.company.CompanyStaff;
import com.deltahomes.backend.entity.enums.AppointmentStatus;
import com.deltahomes.backend.entity.enums.BroadcastType;
import com.deltahomes.backend.entity.enums.CompanyType;
import com.deltahomes.backend.entity.enums.EntityType;
import com.deltahomes.backend.entity.enums.FinishingLevel;
import com.deltahomes.backend.entity.enums.FraudFlagType;
import com.deltahomes.backend.entity.enums.MessageType;
import com.deltahomes.backend.entity.enums.NotificationType;
import com.deltahomes.backend.entity.enums.PaymentStatus;
import com.deltahomes.backend.entity.enums.PropertyPurpose;
import com.deltahomes.backend.entity.enums.PropertyStatus;
import com.deltahomes.backend.entity.enums.Readiness;
import com.deltahomes.backend.entity.enums.ReportStatus;
import com.deltahomes.backend.entity.enums.SubscriptionStatus;
import com.deltahomes.backend.entity.enums.SubscriptionTier;
import com.deltahomes.backend.entity.enums.UserRole;
import com.deltahomes.backend.entity.enums.UserStatus;
import com.deltahomes.backend.entity.enums.VerificationStatus;
import com.deltahomes.backend.entity.enums.VerificationType;
import com.deltahomes.backend.entity.location.City;
import com.deltahomes.backend.entity.location.District;
import com.deltahomes.backend.entity.location.Feature;
import com.deltahomes.backend.entity.location.Service;
import com.deltahomes.backend.entity.marketing.Broadcast;
import com.deltahomes.backend.entity.marketing.BroadcastDelivery;
import com.deltahomes.backend.entity.marketing.FollowerPreference;
import com.deltahomes.backend.entity.moderation.FraudFlag;
import com.deltahomes.backend.entity.moderation.Report;
import com.deltahomes.backend.entity.property.Property;
import com.deltahomes.backend.entity.property.PropertyImage;
import com.deltahomes.backend.entity.property.PropertyVideo;
import com.deltahomes.backend.entity.user.AdminRole;
import com.deltahomes.backend.entity.user.AdminRoleUser;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.entity.user.Verification;
import com.deltahomes.backend.repository.AdminRoleRepository;
import com.deltahomes.backend.repository.AdminRoleUserRepository;
import com.deltahomes.backend.repository.AppointmentRepository;
import com.deltahomes.backend.repository.AuditLogRepository;
import com.deltahomes.backend.repository.BroadcastDeliveryRepository;
import com.deltahomes.backend.repository.BroadcastRepository;
import com.deltahomes.backend.repository.CityRepository;
import com.deltahomes.backend.repository.CmsPageRepository;
import com.deltahomes.backend.repository.CompanyPortfolioRepository;
import com.deltahomes.backend.repository.CompanyRepository;
import com.deltahomes.backend.repository.CompanyServiceRepository;
import com.deltahomes.backend.repository.CompanyStaffRepository;
import com.deltahomes.backend.repository.ConversationRepository;
import com.deltahomes.backend.repository.CouponRepository;
import com.deltahomes.backend.repository.DistrictRepository;
import com.deltahomes.backend.repository.FeatureFlagRepository;
import com.deltahomes.backend.repository.FeatureRepository;
import com.deltahomes.backend.repository.FollowerPreferenceRepository;
import com.deltahomes.backend.repository.FollowerRepository;
import com.deltahomes.backend.repository.FraudFlagRepository;
import com.deltahomes.backend.repository.MessageRepository;
import com.deltahomes.backend.repository.NotificationRepository;
import com.deltahomes.backend.repository.PaymentRepository;
import com.deltahomes.backend.repository.PropertyImageRepository;
import com.deltahomes.backend.repository.PropertyRepository;
import com.deltahomes.backend.repository.PropertyVideoRepository;
import com.deltahomes.backend.repository.ReportRepository;
import com.deltahomes.backend.repository.ReviewRepository;
import com.deltahomes.backend.repository.SavedItemRepository;
import com.deltahomes.backend.repository.ServiceRepository;
import com.deltahomes.backend.repository.SubscriptionPlanRepository;
import com.deltahomes.backend.repository.SubscriptionRepository;
import com.deltahomes.backend.repository.UserRepository;
import com.deltahomes.backend.repository.VerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Demo data seeder.
 * <p>
 * Ensures every table ends up with at least {@code app.seed.target} (default 5)
 * realistic records using deterministic UUIDs, Egyptian identity (Delta region:
 * Mansoura / Damietta / New Damietta / Talkha / Ras El Bar), and fully valid
 * cross-table relationships.
 * <p>
 * Idempotent: rows are only inserted when their fixed UUID (or unique business
 * key for users) is not already present, so re-running on any startup is safe.
 * Disable with {@code app.seed.enabled=false}.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final FeatureRepository featureRepository;
    private final ServiceRepository serviceRepository;
    private final SubscriptionPlanRepository planRepository;
    private final AdminRoleRepository adminRoleRepository;
    private final AdminRoleUserRepository adminRoleUserRepository;
    private final CompanyRepository companyRepository;
    private final CompanyServiceRepository companyServiceRepository;
    private final CompanyStaffRepository companyStaffRepository;
    private final CompanyPortfolioRepository companyPortfolioRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final PropertyVideoRepository propertyVideoRepository;
    private final VerificationRepository verificationRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    private final SavedItemRepository savedItemRepository;
    private final FollowerRepository followerRepository;
    private final FollowerPreferenceRepository followerPreferenceRepository;
    private final BroadcastRepository broadcastRepository;
    private final BroadcastDeliveryRepository broadcastDeliveryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final CouponRepository couponRepository;
    private final ReportRepository reportRepository;
    private final FraudFlagRepository fraudFlagRepository;
    private final FeatureFlagRepository featureFlagRepository;
    private final CmsPageRepository cmsPageRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.target:5}")
    private int target;

    public DataSeeder(UserRepository userRepository,
                      CityRepository cityRepository,
                      DistrictRepository districtRepository,
                      FeatureRepository featureRepository,
                      ServiceRepository serviceRepository,
                      SubscriptionPlanRepository planRepository,
                      AdminRoleRepository adminRoleRepository,
                      AdminRoleUserRepository adminRoleUserRepository,
                      CompanyRepository companyRepository,
                      CompanyServiceRepository companyServiceRepository,
                      CompanyStaffRepository companyStaffRepository,
                      CompanyPortfolioRepository companyPortfolioRepository,
                      PropertyRepository propertyRepository,
                      PropertyImageRepository propertyImageRepository,
                      PropertyVideoRepository propertyVideoRepository,
                      VerificationRepository verificationRepository,
                      ConversationRepository conversationRepository,
                      MessageRepository messageRepository,
                      NotificationRepository notificationRepository,
                      AppointmentRepository appointmentRepository,
                      ReviewRepository reviewRepository,
                      SavedItemRepository savedItemRepository,
                      FollowerRepository followerRepository,
                      FollowerPreferenceRepository followerPreferenceRepository,
                      BroadcastRepository broadcastRepository,
                      BroadcastDeliveryRepository broadcastDeliveryRepository,
                      SubscriptionRepository subscriptionRepository,
                      PaymentRepository paymentRepository,
                      CouponRepository couponRepository,
                      ReportRepository reportRepository,
                      FraudFlagRepository fraudFlagRepository,
                      FeatureFlagRepository featureFlagRepository,
                      CmsPageRepository cmsPageRepository,
                      AuditLogRepository auditLogRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.featureRepository = featureRepository;
        this.serviceRepository = serviceRepository;
        this.planRepository = planRepository;
        this.adminRoleRepository = adminRoleRepository;
        this.adminRoleUserRepository = adminRoleUserRepository;
        this.companyRepository = companyRepository;
        this.companyServiceRepository = companyServiceRepository;
        this.companyStaffRepository = companyStaffRepository;
        this.companyPortfolioRepository = companyPortfolioRepository;
        this.propertyRepository = propertyRepository;
        this.propertyImageRepository = propertyImageRepository;
        this.propertyVideoRepository = propertyVideoRepository;
        this.verificationRepository = verificationRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.notificationRepository = notificationRepository;
        this.appointmentRepository = appointmentRepository;
        this.reviewRepository = reviewRepository;
        this.savedItemRepository = savedItemRepository;
        this.followerRepository = followerRepository;
        this.followerPreferenceRepository = followerPreferenceRepository;
        this.broadcastRepository = broadcastRepository;
        this.broadcastDeliveryRepository = broadcastDeliveryRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.couponRepository = couponRepository;
        this.reportRepository = reportRepository;
        this.fraudFlagRepository = fraudFlagRepository;
        this.featureFlagRepository = featureFlagRepository;
        this.cmsPageRepository = cmsPageRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("[seeder] Disabled via app.seed.enabled=false");
            return;
        }

        List<City> cities = seedCities();
        List<District> districts = seedDistricts(cities);
        List<Feature> features = seedFeatures();
        List<Service> services = seedServices();
        List<SubscriptionPlan> plans = seedPlans();
        List<AdminRole> roles = seedAdminRoles();

        List<User> users = seedUsers(cities);
        seedAdminRoleUsers(roles, users);

        List<Company> companies = seedCompanies(users);
        seedCompanyServices(companies, services);
        seedCompanyStaff(companies, users);
        seedCompanyPortfolio(companies);

        List<Property> properties = seedProperties(users, cities, districts);
        seedPropertyImages(properties);
        seedPropertyVideos(properties);

        seedVerifications(users);
        List<Conversation> conversations = seedConversations(users);
        seedMessages(conversations, users);
        seedNotifications(users);
        List<Appointment> appointments = seedAppointments(properties, users);
        seedReviews(users, appointments);

        List<Broadcast> broadcasts = seedBroadcasts(companies);
        seedBroadcastDeliveries(broadcasts, users);
        seedFollowerPreferences(users, companies);
        seedFollowers(users, companies);

        List<Subscription> subscriptions = seedSubscriptions(companies, plans, users);
        seedPayments(subscriptions);
        seedCoupons();
        seedSavedItems(users, properties, companies);
        seedReports(users);
        seedFraudFlags(properties, companies);
        seedFeatureFlags();
        seedCmsPages();
        seedAuditLogs(users);

        log.info("[seeder] Demo data is ready (target={} rows per table).", target);
    }

    // ------------------------------------------------------------------
    // Lookups
    // ------------------------------------------------------------------

    private List<City> seedCities() {
        List<City> result = new ArrayList<>();
        // Existing bootstrap city (Mansoura), kept by its fixed UUID.
        seed(result, cityRepository, "11111111-1111-1111-1111-111111111111",
                () -> city("Mansoura", "المنصورة"));
        seed(result, cityRepository, "10000000-0000-0000-0000-000000000002",
                () -> city("Damietta", "دمياط"));
        seed(result, cityRepository, "10000000-0000-0000-0000-000000000003",
                () -> city("New Damietta", "دمياط الجديدة"));
        seed(result, cityRepository, "10000000-0000-0000-0000-000000000004",
                () -> city("Talkha", "طلخا"));
        seed(result, cityRepository, "10000000-0000-0000-0000-000000000005",
                () -> city("Ras El Bar", "رأس البر"));
        return result;
    }

    private List<District> seedDistricts(List<City> cities) {
        List<District> result = new ArrayList<>();
        // Existing bootstrap district (Downtown), kept by its fixed UUID.
        seed(result, districtRepository, "22222222-2222-2222-2222-222222222222",
                () -> district(city(cities, "Mansoura"), "Downtown", "وسط البلد"));
        seed(result, districtRepository, "20000000-0000-0000-0000-000000000002",
                () -> district(city(cities, "Mansoura"), "Toriel", "طوريل"));
        seed(result, districtRepository, "20000000-0000-0000-0000-000000000003",
                () -> district(city(cities, "New Damietta"), "Corniche", "الكورنيش"));
        seed(result, districtRepository, "20000000-0000-0000-0000-000000000004",
                () -> district(city(cities, "Damietta"), "El-Tahrir", "التحرير"));
        seed(result, districtRepository, "20000000-0000-0000-0000-000000000005",
                () -> district(city(cities, "Ras El Bar"), "Ras El Bar Center", "وسط رأس البر"));
        return result;
    }

    private List<Feature> seedFeatures() {
        List<Feature> result = new ArrayList<>();
        seed(result, featureRepository, "30000000-0000-0000-0000-000000000001",
                () -> feature("Elevator", "مصعد", "BOOLEAN"));
        seed(result, featureRepository, "30000000-0000-0000-0000-000000000002",
                () -> feature("Private Garden", "حديقة خاصة", "BOOLEAN"));
        seed(result, featureRepository, "30000000-0000-0000-0000-000000000003",
                () -> feature("Air Conditioning", "تكييف مركزي", "BOOLEAN"));
        seed(result, featureRepository, "30000000-0000-0000-0000-000000000004",
                () -> feature("Parking", "جراج خاص", "BOOLEAN"));
        seed(result, featureRepository, "30000000-0000-0000-0000-000000000005",
                () -> feature("Roof Terrace", "سطح", "BOOLEAN"));
        return result;
    }

    private List<Service> seedServices() {
        List<Service> result = new ArrayList<>();
        seed(result, serviceRepository, "40000000-0000-0000-0000-000000000001",
                () -> service("Interior Painting", "دهانات داخلية", "FINISHING"));
        seed(result, serviceRepository, "40000000-0000-0000-0000-000000000002",
                () -> service("Flooring & Tiling", "أرضيات وسيراميك", "FINISHING"));
        seed(result, serviceRepository, "40000000-0000-0000-0000-000000000003",
                () -> service("Plumbing Works", "أعمال سباكة", "PLUMBING"));
        seed(result, serviceRepository, "40000000-0000-0000-0000-000000000004",
                () -> service("Electrical Works", "أعمال كهرباء", "ELECTRICAL"));
        seed(result, serviceRepository, "40000000-0000-0000-0000-000000000005",
                () -> service("Facade Cladding", "واجهات وتكسية", "MAINTENANCE"));
        return result;
    }

    private List<SubscriptionPlan> seedPlans() {
        List<SubscriptionPlan> result = new ArrayList<>();
        seed(result, planRepository, "50000000-0000-0000-0000-000000000001",
                () -> plan("Basic", SubscriptionTier.BASIC, "0", 1, 0));
        seed(result, planRepository, "50000000-0000-0000-0000-000000000002",
                () -> plan("Silver", SubscriptionTier.BASIC, "299", 5, 5));
        seed(result, planRepository, "50000000-0000-0000-0000-000000000003",
                () -> plan("Gold", SubscriptionTier.PREMIUM, "599", 20, 20));
        seed(result, planRepository, "50000000-0000-0000-0000-000000000004",
                () -> plan("Platinum", SubscriptionTier.PREMIUM, "999", 50, 50));
        seed(result, planRepository, "50000000-0000-0000-0000-000000000005",
                () -> plan("Enterprise", SubscriptionTier.ENTERPRISE, "2499", 200, 200));
        return result;
    }

    private List<AdminRole> seedAdminRoles() {
        List<AdminRole> result = new ArrayList<>();
        seed(result, adminRoleRepository, "60000000-0000-0000-0000-000000000001",
                () -> adminRole("SUPER_ADMIN"));
        seed(result, adminRoleRepository, "60000000-0000-0000-0000-000000000002",
                () -> adminRole("MODERATOR"));
        seed(result, adminRoleRepository, "60000000-0000-0000-0000-000000000003",
                () -> adminRole("CONTENT_MANAGER"));
        seed(result, adminRoleRepository, "60000000-0000-0000-0000-000000000004",
                () -> adminRole("FINANCE"));
        seed(result, adminRoleRepository, "60000000-0000-0000-0000-000000000005",
                () -> adminRole("SUPPORT"));
        return result;
    }

    // ------------------------------------------------------------------
    // Users & companies
    // ------------------------------------------------------------------

    private List<User> seedUsers(List<City> cities) {
        List<User> users = new ArrayList<>();
        users.add(ensureUser("Customer One", "01011111111", "customer1@example.com", UserRole.CUSTOMER));
        users.add(ensureUser("Customer Two", "01022222222", "customer2@example.com", UserRole.CUSTOMER));
        users.add(ensureUser("Owner One", "01033333333", "owner1@example.com", UserRole.OWNER));
        users.add(ensureUser("Owner Two", "01044444444", "owner2@example.com", UserRole.OWNER));
        users.add(ensureUser("Office One", "01055555555", "office1@example.com", UserRole.OFFICE));
        users.add(ensureUser("Office Two", "01066666666", "office2@example.com", UserRole.OFFICE));
        users.add(ensureUser("Company One", "01077777777", "company1@example.com", UserRole.COMPANY));
        users.add(ensureUser("Company Two", "01088888888", "company2@example.com", UserRole.COMPANY));
        users.add(ensureUser("Technician One", "01099999999", "technician1@example.com", UserRole.TECHNICIAN));
        users.add(ensureUser("Technician Two", "01000000000", "technician2@example.com", UserRole.TECHNICIAN));
        User admin = userRepository.findByPhone("01026962089").orElseGet(() ->
                userRepository.findAll().stream()
                        .filter(u -> u.getRole() == UserRole.ADMIN)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No admin user found")));
        users.add(admin);

        City newDamietta = city(cities, "New Damietta");
        City mansoura = city(cities, "Mansoura");
        City rasElBar = city(cities, "Ras El Bar");
        City damietta = city(cities, "Damietta");
        if (cityRepository.count() <= target && users.size() >= target) {
            // Give the curated users a home city so profiles look complete.
            User[] withCity = {users.get(0), users.get(1), users.get(2), users.get(3), users.get(4), users.get(5)};
            City[] cityRef = {newDamietta, mansoura, damietta, rasElBar, mansoura, newDamietta};
            for (int i = 0; i < withCity.length; i++) {
                if (withCity[i].getCity() == null) {
                    withCity[i].setCity(cityRef[i]);
                    userRepository.save(withCity[i]);
                }
            }
        }
        return users;
    }

    private List<Company> seedCompanies(List<User> users) {
        User companyOne = userByRole(users, "company1@example.com");
        User companyTwo = userByRole(users, "company2@example.com");
        User officeOne = userByRole(users, "office1@example.com");

        List<Company> result = new ArrayList<>();
        // Existing bootstrap company, kept by its fixed UUID.
        seed(result, companyRepository, "33333333-3333-3333-3333-333333333333",
                () -> company(admin(users), CompanyType.FINISHING_COMPANY, "Delta Finishing Co.",
                        "01011222333", "01011222333", "info@deltafinishing.com", 4.9));
        seed(result, companyRepository, "80000000-0000-0000-0000-000000000001",
                () -> company(companyOne, CompanyType.FINISHING_COMPANY, "El-Delta Finishing Group",
                        "01055667788", "01055667788", "sales@eldelta-group.com", 4.8));
        seed(result, companyRepository, "80000000-0000-0000-0000-000000000002",
                () -> company(companyTwo, CompanyType.FINISHING_COMPANY, "Damietta Star Finishing",
                        "01122334455", "01122334455", "hello@damiettastar.com", 4.7));
        seed(result, companyRepository, "80000000-0000-0000-0000-000000000003",
                () -> company(companyTwo, CompanyType.MAINTENANCE_PROVIDER, "Nile Maintenance & Services",
                        "01099887766", "01099887766", "support@nilemaint.com", 4.6));
        seed(result, companyRepository, "80000000-0000-0000-0000-000000000004",
                () -> company(officeOne, CompanyType.REAL_ESTATE_OFFICE, "Delta Prime Real Estate",
                        "01233445566", "01233445566", "info@deltaprime.com", 4.9));
        return result;
    }

    private void seedCompanyServices(List<Company> companies, List<Service> services) {
        Company deltaFinishing = company(companies, "Delta Finishing Co.");
        Company elDelta = company(companies, "El-Delta Finishing Group");
        Company star = company(companies, "Damietta Star Finishing");
        Company nile = company(companies, "Nile Maintenance & Services");

        List<CompanyService> rows = new ArrayList<>();
        seed(rows, companyServiceRepository, "93000000-0000-0000-0000-000000000001",
                () -> cs(deltaFinishing, service(services, "Interior Painting")));
        seed(rows, companyServiceRepository, "93000000-0000-0000-0000-000000000002",
                () -> cs(deltaFinishing, service(services, "Flooring & Tiling")));
        seed(rows, companyServiceRepository, "93000000-0000-0000-0000-000000000003",
                () -> cs(elDelta, service(services, "Interior Painting")));
        seed(rows, companyServiceRepository, "93000000-0000-0000-0000-000000000004",
                () -> cs(nile, service(services, "Plumbing Works")));
        seed(rows, companyServiceRepository, "93000000-0000-0000-0000-000000000005",
                () -> cs(star, service(services, "Electrical Works")));
    }

    private void seedCompanyStaff(List<Company> companies, List<User> users) {
        Company deltaFinishing = company(companies, "Delta Finishing Co.");
        Company elDelta = company(companies, "El-Delta Finishing Group");
        Company nile = company(companies, "Nile Maintenance & Services");
        Company prime = company(companies, "Delta Prime Real Estate");

        User companyTwo = userByRole(users, "company2@example.com");
        User techOne = userByRole(users, "technician1@example.com");
        User techTwo = userByRole(users, "technician2@example.com");
        User officeTwo = userByRole(users, "office2@example.com");

        List<CompanyStaff> rows = new ArrayList<>();
        seed(rows, companyStaffRepository, "94000000-0000-0000-0000-000000000001",
                () -> staff(deltaFinishing, companyTwo, "Operations Manager"));
        seed(rows, companyStaffRepository, "94000000-0000-0000-0000-000000000002",
                () -> staff(elDelta, techOne, "Site Supervisor"));
        seed(rows, companyStaffRepository, "94000000-0000-0000-0000-000000000003",
                () -> staff(elDelta, techTwo, "Foreman"));
        seed(rows, companyStaffRepository, "94000000-0000-0000-0000-000000000004",
                () -> staff(nile, techOne, "Senior Technician"));
        seed(rows, companyStaffRepository, "94000000-0000-0000-0000-000000000005",
                () -> staff(prime, officeTwo, "Sales Agent"));
    }

    private void seedCompanyPortfolio(List<Company> companies) {
        Company elDelta = company(companies, "El-Delta Finishing Group");
        Company star = company(companies, "Damietta Star Finishing");
        Company nile = company(companies, "Nile Maintenance & Services");
        Company prime = company(companies, "Delta Prime Real Estate");
        Company deltaFinishing = company(companies, "Delta Finishing Co.");

        List<CompanyPortfolio> rows = new ArrayList<>();
        seed(rows, companyPortfolioRepository, "95000000-0000-0000-0000-000000000001",
                () -> portfolio(elDelta, "Kitchen renovation - New Damietta",
                        LocalDate.of(2025, 3, 10)));
        seed(rows, companyPortfolioRepository, "95000000-0000-0000-0000-000000000002",
                () -> portfolio(star, "Full finishing - Mansoura apartment",
                        LocalDate.of(2025, 5, 22)));
        seed(rows, companyPortfolioRepository, "95000000-0000-0000-0000-000000000003",
                () -> portfolio(nile, "Complete plumbing overhaul - Talkha villa",
                        LocalDate.of(2025, 7, 1)));
        seed(rows, companyPortfolioRepository, "95000000-0000-0000-0000-000000000004",
                () -> portfolio(prime, "Sold - sea view apartment Ras El Bar",
                        LocalDate.of(2025, 8, 15)));
        seed(rows, companyPortfolioRepository, "95000000-0000-0000-0000-000000000005",
                () -> portfolio(deltaFinishing, "Facade cladding - Damietta Corniche",
                        LocalDate.of(2025, 9, 5)));
    }

    // ------------------------------------------------------------------
    // Properties
    // ------------------------------------------------------------------

    private List<Property> seedProperties(List<User> users, List<City> cities, List<District> districts) {
        User ownerOne = userByRole(users, "owner1@example.com");
        User ownerTwo = userByRole(users, "owner2@example.com");
        User admin = admin(users);

        City mansoura = city(cities, "Mansoura");
        City newDamietta = city(cities, "New Damietta");
        City damietta = city(cities, "Damietta");
        City rasElBar = city(cities, "Ras El Bar");
        District downtown = district(districts, "Downtown");
        District toriel = district(districts, "Toriel");
        District corniche = district(districts, "Corniche");
        District elTahrir = district(districts, "El-Tahrir");
        District rasElBarCenter = district(districts, "Ras El Bar Center");

        List<Property> result = new ArrayList<>();
        // The existing bootstrap property is appended LAST so the crafted rows
        // (indexes 0-4) receive the five property images / videos.
        seed(result, propertyRepository, "90000000-0000-0000-0000-000000000001",
                () -> property(ownerOne, "Sea View Apartment - Ras El Bar", "15000.00",
                        PropertyPurpose.RENT, "APARTMENT", rasElBar, rasElBarCenter,
                        "31.5275", "31.8385", FinishingLevel.LUXURY, true));
        seed(result, propertyRepository, "90000000-0000-0000-0000-000000000002",
                () -> property(ownerTwo, "Modern Duplex in New Damietta", "3250000.00",
                        PropertyPurpose.SALE, "DUPLEX", newDamietta, corniche,
                        "31.4430", "31.6730", FinishingLevel.LUXURY, true));
        seed(result, propertyRepository, "90000000-0000-0000-0000-000000000003",
                () -> property(ownerOne, "Family Villa in Damietta", "5500000.00",
                        PropertyPurpose.SALE, "VILLA", damietta, elTahrir,
                        "31.4175", "31.8144", FinishingLevel.FINISHED, false));
        seed(result, propertyRepository, "90000000-0000-0000-0000-000000000004",
                () -> property(ownerTwo, "Studio in Toriel - Mansoura", "950000.00",
                        PropertyPurpose.SALE, "APARTMENT", mansoura, toriel,
                        "31.0300", "31.3550", FinishingLevel.SEMI_FINISHED, false));
        seed(result, propertyRepository, "90000000-0000-0000-0000-000000000005",
                () -> property(ownerOne, "Ground Floor with Garden - Toriel", "1850000.00",
                        PropertyPurpose.SALE, "APARTMENT", mansoura, toriel,
                        "31.0280", "31.3620", FinishingLevel.FINISHED, false));
        seed(result, propertyRepository, "44444444-4444-4444-4444-444444444444",
                () -> property(admin, "Nice Apartment", "1250000.00",
                        PropertyPurpose.SALE, "APARTMENT", mansoura, downtown,
                        "31.0419", "31.3785", FinishingLevel.FINISHED, true));
        return result;
    }

    private void seedPropertyImages(List<Property> properties) {
        List<PropertyImage> rows = new ArrayList<>();
        String[] seeds = {"delta1", "delta2", "delta3", "delta4", "delta5"};
        for (int i = 0; i < 5 && i < properties.size(); i++) {
            Property p = properties.get(i);
            String id = "91000000-0000-0000-0000-00000000000" + (i + 1);
            String seedVal = seeds[i];
            seed(rows, propertyImageRepository, id,
                    () -> image(p, "https://picsum.photos/seed/" + seedVal + "/800/600", 1));
        }
    }

    private void seedPropertyVideos(List<Property> properties) {
        List<PropertyVideo> rows = new ArrayList<>();
        for (int i = 0; i < 5 && i < properties.size(); i++) {
            Property p = properties.get(i);
            int videoIdx = i + 1;
            String id = "92000000-0000-0000-0000-00000000000" + videoIdx;
            seed(rows, propertyVideoRepository, id,
                    () -> video(p, "https://www.w3schools.com/html/mov_bbb.mp4",
                            "https://picsum.photos/seed/video" + videoIdx + "/640/360", 35));
        }
    }

    // ------------------------------------------------------------------
    // Verification & communication
    // ------------------------------------------------------------------

    private void seedVerifications(List<User> users) {
        User companyOne = userByRole(users, "company1@example.com");
        User companyTwo = userByRole(users, "company2@example.com");
        User ownerOne = userByRole(users, "owner1@example.com");
        User techOne = userByRole(users, "technician1@example.com");
        User officeOne = userByRole(users, "office1@example.com");
        User admin = admin(users);

        List<Verification> rows = new ArrayList<>();
        seed(rows, verificationRepository, "a1000000-0000-0000-0000-000000000001",
                () -> verification(companyOne, VerificationType.COMMERCIAL_REGISTRY,
                        VerificationStatus.ACCEPTED, admin));
        seed(rows, verificationRepository, "a1000000-0000-0000-0000-000000000002",
                () -> verification(companyTwo, VerificationType.NATIONAL_ID,
                        VerificationStatus.ACCEPTED, admin));
        seed(rows, verificationRepository, "a1000000-0000-0000-0000-000000000003",
                () -> verification(ownerOne, VerificationType.NATIONAL_ID,
                        VerificationStatus.PENDING, null));
        seed(rows, verificationRepository, "a1000000-0000-0000-0000-000000000004",
                () -> verification(techOne, VerificationType.NATIONAL_ID,
                        VerificationStatus.PENDING, null));
        seed(rows, verificationRepository, "a1000000-0000-0000-0000-000000000005",
                () -> verification(officeOne, VerificationType.COMMERCIAL_REGISTRY,
                        VerificationStatus.ACCEPTED, admin));
    }

    private void seedAdminRoleUsers(List<AdminRole> roles, List<User> users) {
        User admin = admin(users);
        List<AdminRoleUser> rows = new ArrayList<>();
        for (int i = 0; i < roles.size(); i++) {
            AdminRole role = roles.get(i);
            String id = "a2000000-0000-0000-0000-00000000000" + (i + 1);
            seed(rows, adminRoleUserRepository, id, () -> roleUser(role, admin));
        }
    }

    private List<Conversation> seedConversations(List<User> users) {
        User customerOne = userByRole(users, "customer1@example.com");
        User customerTwo = userByRole(users, "customer2@example.com");
        User ownerOne = userByRole(users, "owner1@example.com");
        User ownerTwo = userByRole(users, "owner2@example.com");
        User companyOne = userByRole(users, "company1@example.com");
        User companyTwo = userByRole(users, "company2@example.com");
        User techOne = userByRole(users, "technician1@example.com");

        List<Conversation> result = new ArrayList<>();
        seed(result, conversationRepository, "a6000000-0000-0000-0000-000000000001",
                () -> conversation(customerOne, ownerOne, "Yes, you can visit tomorrow."));
        seed(result, conversationRepository, "a6000000-0000-0000-0000-000000000002",
                () -> conversation(customerOne, companyOne, "Yes, including materials."));
        seed(result, conversationRepository, "a6000000-0000-0000-0000-000000000003",
                () -> conversation(customerTwo, ownerTwo, "The price is negotiable."));
        seed(result, conversationRepository, "a6000000-0000-0000-0000-000000000004",
                () -> conversation(customerTwo, companyTwo, "We can send an engineer."));
        seed(result, conversationRepository, "a6000000-0000-0000-0000-000000000005",
                () -> conversation(customerOne, techOne, "I will call you in the morning."));
        return result;
    }

    private void seedMessages(List<Conversation> conversations, List<User> users) {
        User customerOne = userByRole(users, "customer1@example.com");
        User customerTwo = userByRole(users, "customer2@example.com");
        User ownerOne = userByRole(users, "owner1@example.com");
        User companyOne = userByRole(users, "company1@example.com");

        List<Message> rows = new ArrayList<>();
        seed(rows, messageRepository, "a7000000-0000-0000-0000-000000000001",
                () -> message(conversations.get(0), customerOne, "Is the apartment still available?"));
        seed(rows, messageRepository, "a7000000-0000-0000-0000-000000000002",
                () -> message(conversations.get(0), ownerOne, "Yes, you can visit tomorrow."));
        seed(rows, messageRepository, "a7000000-0000-0000-0000-000000000003",
                () -> message(conversations.get(1), customerOne, "Do you provide full finishing packages?"));
        seed(rows, messageRepository, "a7000000-0000-0000-0000-000000000004",
                () -> message(conversations.get(1), companyOne, "Yes, including materials."));
        seed(rows, messageRepository, "a7000000-0000-0000-0000-000000000005",
                () -> message(conversations.get(2), customerTwo, "What is the rental price?"));
    }

    private void seedNotifications(List<User> users) {
        User customerOne = userByRole(users, "customer1@example.com");
        User customerTwo = userByRole(users, "customer2@example.com");
        User ownerOne = userByRole(users, "owner1@example.com");
        User companyOne = userByRole(users, "company1@example.com");

        List<Notification> rows = new ArrayList<>();
        seed(rows, notificationRepository, "a8000000-0000-0000-0000-000000000001",
                () -> notification(customerOne, "New property in your area",
                        "A new 3-bedroom apartment was listed in Toriel.", NotificationType.MARKETING,
                        EntityType.PROPERTY, "90000000-0000-0000-0000-000000000004", false));
        seed(rows, notificationRepository, "a8000000-0000-0000-0000-000000000002",
                () -> notification(customerOne, "Appointment accepted",
                        "Your visit to the Modern Duplex was confirmed.", NotificationType.PERSONAL,
                        EntityType.PROPERTY, "90000000-0000-0000-0000-000000000002", true));
        seed(rows, notificationRepository, "a8000000-0000-0000-0000-000000000003",
                () -> notification(customerTwo, "Broadcast from Delta Finishing Co.",
                        "Summer finishing discount is live for 2 more weeks.", NotificationType.MARKETING,
                        EntityType.COMPANY, "33333333-3333-3333-3333-333333333333", false));
        seed(rows, notificationRepository, "a8000000-0000-0000-0000-000000000004",
                () -> notification(ownerOne, "Someone saved your property",
                        "A customer saved your Ground Floor with Garden listing.", NotificationType.PERSONAL,
                        EntityType.PROPERTY, "90000000-0000-0000-0000-000000000005", true));
        seed(rows, notificationRepository, "a8000000-0000-0000-0000-000000000005",
                () -> notification(companyOne, "New follower",
                        "Customer One started following your company.", NotificationType.SYSTEM,
                        EntityType.COMPANY, "80000000-0000-0000-0000-000000000001", false));
    }

    private List<Appointment> seedAppointments(List<Property> properties, List<User> users) {
        User customerOne = userByRole(users, "customer1@example.com");
        User customerTwo = userByRole(users, "customer2@example.com");
        User ownerOne = userByRole(users, "owner1@example.com");
        User ownerTwo = userByRole(users, "owner2@example.com");

        Property duplex = property(properties, "Modern Duplex in New Damietta");
        Property seaView = property(properties, "Sea View Apartment - Ras El Bar");
        Property studio = property(properties, "Studio in Toriel - Mansoura");
        Property groundFloor = property(properties, "Ground Floor with Garden - Toriel");
        Property niceApartment = property(properties, "Nice Apartment");

        LocalDateTime now = LocalDateTime.now();
        List<Appointment> result = new ArrayList<>();
        seed(result, appointmentRepository, "a9000000-0000-0000-0000-000000000001",
                () -> appointment(duplex, customerOne, ownerOne,
                        now.plusDays(2).withHour(11).withMinute(0), AppointmentStatus.ACCEPTED,
                        "Interested in visiting this weekend"));
        seed(result, appointmentRepository, "a9000000-0000-0000-0000-000000000002",
                () -> appointment(seaView, customerTwo, ownerTwo,
                        now.plusDays(3).withHour(16).withMinute(30), AppointmentStatus.PENDING,
                        "Please show the sea view rooms"));
        seed(result, appointmentRepository, "a9000000-0000-0000-0000-000000000003",
                () -> appointment(studio, customerOne, ownerOne,
                        now.minusDays(5).withHour(12).withMinute(0), AppointmentStatus.COMPLETED,
                        "Short visit to check finishing"));
        seed(result, appointmentRepository, "a9000000-0000-0000-0000-000000000004",
                () -> appointment(groundFloor, customerTwo, ownerOne,
                        now.plusDays(5).withHour(10).withMinute(0), AppointmentStatus.REJECTED,
                        "Owner unavailable that day"));
        seed(result, appointmentRepository, "a9000000-0000-0000-0000-000000000005",
                () -> appointment(niceApartment, customerTwo, admin(users),
                        now.plusDays(1).withHour(14).withMinute(0), AppointmentStatus.PENDING,
                        "Would like a quick viewing"));
        return result;
    }

    private void seedReviews(List<User> users, List<Appointment> appointments) {
        User customerOne = userByRole(users, "customer1@example.com");
        User customerTwo = userByRole(users, "customer2@example.com");
        User techOne = userByRole(users, "technician1@example.com");

        List<Review> rows = new ArrayList<>();
        seed(rows, reviewRepository, "a4000000-0000-0000-0000-000000000001",
                () -> review(customerOne, EntityType.COMPANY, "33333333-3333-3333-3333-333333333333",
                        (byte) 5, "Excellent finishing work, very professional team.", true,
                        appointments.get(2).getId()));
        seed(rows, reviewRepository, "a4000000-0000-0000-0000-000000000002",
                () -> review(customerOne, EntityType.PROPERTY, "90000000-0000-0000-0000-000000000002",
                        (byte) 4, "Great duplex, sea view is amazing.", false, null));
        seed(rows, reviewRepository, "a4000000-0000-0000-0000-000000000003",
                () -> review(customerTwo, EntityType.COMPANY, "80000000-0000-0000-0000-000000000002",
                        (byte) 4, "Good work, slightly delayed delivery.", false, null));
        seed(rows, reviewRepository, "a4000000-0000-0000-0000-000000000004",
                () -> review(customerTwo, EntityType.PROPERTY, "90000000-0000-0000-0000-000000000001",
                        (byte) 5, "Perfect location right on the beach.", true,
                        appointments.get(1).getId()));
        seed(rows, reviewRepository, "a4000000-0000-0000-0000-000000000005",
                () -> review(techOne, EntityType.PROPERTY, "90000000-0000-0000-0000-000000000004",
                        (byte) 3, "Fair price for a studio, needs finishing.", false, null));
    }

    // ------------------------------------------------------------------
    // Marketing & commerce
    // ------------------------------------------------------------------

    private List<Broadcast> seedBroadcasts(List<Company> companies) {
        Company deltaFinishing = company(companies, "Delta Finishing Co.");
        Company elDelta = company(companies, "El-Delta Finishing Group");
        Company star = company(companies, "Damietta Star Finishing");
        Company nile = company(companies, "Nile Maintenance & Services");

        List<Broadcast> result = new ArrayList<>();
        seed(result, broadcastRepository, "b1000000-0000-0000-0000-000000000001",
                () -> broadcast(deltaFinishing, "Summer Finishing Discount",
                        "Get 20% off all finishing packages until the end of the month.",
                        BroadcastType.OFFER));
        seed(result, broadcastRepository, "b1000000-0000-0000-0000-000000000002",
                () -> broadcast(elDelta, "Kitchen Project Completed",
                        "We just finished a modern kitchen in New Damietta - check the portfolio!",
                        BroadcastType.NEWS));
        seed(result, broadcastRepository, "b1000000-0000-0000-0000-000000000003",
                () -> broadcast(star, "New Year Renovation Deal",
                        "Book now and get free electrical works with any finishing package.",
                        BroadcastType.DISCOUNT));
        seed(result, broadcastRepository, "b1000000-0000-0000-0000-000000000004",
                () -> broadcast(nile, "New Listing: Villa Maintenance Plans",
                        "Annual maintenance contracts now available for villas in Damietta.",
                        BroadcastType.NEW_PROPERTY));
        seed(result, broadcastRepository, "b1000000-0000-0000-0000-000000000005",
                () -> broadcast(elDelta, "Watch Our Latest Works",
                        "A short video tour of our latest finishing projects in Mansoura.",
                        BroadcastType.VIDEO));
        return result;
    }

    private void seedBroadcastDeliveries(List<Broadcast> broadcasts, List<User> users) {
        User customerOne = userByRole(users, "customer1@example.com");
        User customerTwo = userByRole(users, "customer2@example.com");
        User ownerOne = userByRole(users, "owner1@example.com");

        List<BroadcastDelivery> rows = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        seed(rows, broadcastDeliveryRepository, "b2000000-0000-0000-0000-000000000001",
                () -> delivery(broadcasts.get(0), customerOne, true, true, now.minusDays(1)));
        seed(rows, broadcastDeliveryRepository, "b2000000-0000-0000-0000-000000000002",
                () -> delivery(broadcasts.get(0), customerTwo, true, false, now.minusDays(1)));
        seed(rows, broadcastDeliveryRepository, "b2000000-0000-0000-0000-000000000003",
                () -> delivery(broadcasts.get(1), customerOne, false, false, null));
        seed(rows, broadcastDeliveryRepository, "b2000000-0000-0000-0000-000000000004",
                () -> delivery(broadcasts.get(2), customerTwo, true, true, now.minusHours(5)));
        seed(rows, broadcastDeliveryRepository, "b2000000-0000-0000-0000-000000000005",
                () -> delivery(broadcasts.get(3), ownerOne, false, false, null));
    }

    private void seedFollowerPreferences(List<User> users, List<Company> companies) {
        User customerOne = userByRole(users, "customer1@example.com");
        User customerTwo = userByRole(users, "customer2@example.com");
        User ownerOne = userByRole(users, "owner1@example.com");
        Company deltaFinishing = company(companies, "Delta Finishing Co.");
        Company elDelta = company(companies, "El-Delta Finishing Group");
        Company nile = company(companies, "Nile Maintenance & Services");

        List<FollowerPreference> rows = new ArrayList<>();
        seed(rows, followerPreferenceRepository, "b3000000-0000-0000-0000-000000000001",
                () -> preference(customerOne, deltaFinishing, true, true, true, true, true));
        seed(rows, followerPreferenceRepository, "b3000000-0000-0000-0000-000000000002",
                () -> preference(customerTwo, deltaFinishing, true, false, true, true, true));
        seed(rows, followerPreferenceRepository, "b3000000-0000-0000-0000-000000000003",
                () -> preference(customerOne, elDelta, true, true, true, true, true));
        seed(rows, followerPreferenceRepository, "b3000000-0000-0000-0000-000000000004",
                () -> preference(customerTwo, nile, false, false, true, false, true));
        seed(rows, followerPreferenceRepository, "b3000000-0000-0000-0000-000000000005",
                () -> preference(ownerOne, elDelta, true, false, true, true, false));
    }

    private void seedFollowers(List<User> users, List<Company> companies) {
        User customerOne = userByRole(users, "customer1@example.com");
        User customerTwo = userByRole(users, "customer2@example.com");
        User ownerOne = userByRole(users, "owner1@example.com");
        Company deltaFinishing = company(companies, "Delta Finishing Co.");
        Company elDelta = company(companies, "El-Delta Finishing Group");
        Company nile = company(companies, "Nile Maintenance & Services");

        List<Follower> rows = new ArrayList<>();
        seed(rows, followerRepository, "a5000000-0000-0000-0000-000000000001",
                () -> follower(customerOne, deltaFinishing));
        seed(rows, followerRepository, "a5000000-0000-0000-0000-000000000002",
                () -> follower(customerTwo, deltaFinishing));
        seed(rows, followerRepository, "a5000000-0000-0000-0000-000000000003",
                () -> follower(customerOne, elDelta));
        seed(rows, followerRepository, "a5000000-0000-0000-0000-000000000004",
                () -> follower(customerTwo, nile));
        seed(rows, followerRepository, "a5000000-0000-0000-0000-000000000005",
                () -> follower(ownerOne, elDelta));
    }

    private List<Subscription> seedSubscriptions(List<Company> companies, List<SubscriptionPlan> plans,
                                                 List<User> users) {
        Company deltaFinishing = company(companies, "Delta Finishing Co.");
        Company elDelta = company(companies, "El-Delta Finishing Group");
        Company star = company(companies, "Damietta Star Finishing");
        Company nile = company(companies, "Nile Maintenance & Services");
        User ownerOne = userByRole(users, "owner1@example.com");

        SubscriptionPlan gold = plan(plans, "Gold");
        SubscriptionPlan platinum = plan(plans, "Platinum");
        SubscriptionPlan enterprise = plan(plans, "Enterprise");
        SubscriptionPlan silver = plan(plans, "Silver");

        LocalDate today = LocalDate.now();
        List<Subscription> result = new ArrayList<>();
        seed(result, subscriptionRepository, "b4000000-0000-0000-0000-000000000001",
                () -> subscription(null, deltaFinishing.getId(), gold,
                        today.minusMonths(6), today.plusMonths(6), SubscriptionStatus.ACTIVE));
        seed(result, subscriptionRepository, "b4000000-0000-0000-0000-000000000002",
                () -> subscription(null, elDelta.getId(), platinum,
                        today.minusMonths(1), today.plusMonths(11), SubscriptionStatus.ACTIVE));
        seed(result, subscriptionRepository, "b4000000-0000-0000-0000-000000000003",
                () -> subscription(null, star.getId(), enterprise,
                        today.minusDays(10), today.plusYears(1), SubscriptionStatus.ACTIVE));
        seed(result, subscriptionRepository, "b4000000-0000-0000-0000-000000000004",
                () -> subscription(null, nile.getId(), gold,
                        today.minusMonths(12), today.minusMonths(6), SubscriptionStatus.EXPIRED));
        seed(result, subscriptionRepository, "b4000000-0000-0000-0000-000000000005",
                () -> subscription(ownerOne.getId(), null, silver,
                        today.minusMonths(2), today.plusMonths(10), SubscriptionStatus.ACTIVE));
        return result;
    }

    private void seedPayments(List<Subscription> subscriptions) {
        List<Payment> rows = new ArrayList<>();
        String[] methods = {"VODAFONE_CASH", "CREDIT_CARD", "BANK_TRANSFER", "CREDIT_CARD", "VODAFONE_CASH"};
        PaymentStatus[] statuses = {PaymentStatus.PAID, PaymentStatus.PAID, PaymentStatus.PAID,
                PaymentStatus.REFUNDED, PaymentStatus.PAID};
        String[] refs = {"VODAFONE-2025-11837", "CC-PAY-88231", "BT-REF-554120", "CC-PAY-44012",
                "VODAFONE-2025-29441"};
        for (int i = 0; i < subscriptions.size(); i++) {
            Subscription sub = subscriptions.get(i);
            String id = "b5000000-0000-0000-0000-00000000000" + (i + 1);
            BigDecimal amount = sub.getPlan() == null ? BigDecimal.ZERO : sub.getPlan().getPrice();
            String method = methods[i];
            PaymentStatus status = statuses[i];
            String ref = refs[i];
            seed(rows, paymentRepository, id, () -> payment(sub, amount, method, status, ref));
        }
    }

    private void seedCoupons() {
        LocalDate today = LocalDate.now();
        List<Coupon> rows = new ArrayList<>();
        seed(rows, couponRepository, "b6000000-0000-0000-0000-000000000001",
                () -> coupon("WELCOME10", (byte) 10, today.minusDays(30), today.plusDays(60), 100));
        seed(rows, couponRepository, "b6000000-0000-0000-0000-000000000002",
                () -> coupon("SUMMER20", (byte) 20, today.minusDays(15), today.plusDays(45), 50));
        seed(rows, couponRepository, "b6000000-0000-0000-0000-000000000003",
                () -> coupon("NEWHOME15", (byte) 15, today.minusDays(60), today.plusDays(90), 200));
        seed(rows, couponRepository, "b6000000-0000-0000-0000-000000000004",
                () -> coupon("FINISH10", (byte) 10, today.minusDays(5), today.plusDays(25), 150));
        seed(rows, couponRepository, "b6000000-0000-0000-0000-000000000005",
                () -> coupon("RENTFREE5", (byte) 5, today.minusDays(10), today.plusDays(20), 80));
    }

    private void seedSavedItems(List<User> users, List<Property> properties, List<Company> companies) {
        User customerOne = userByRole(users, "customer1@example.com");
        User customerTwo = userByRole(users, "customer2@example.com");
        Company deltaFinishing = company(companies, "Delta Finishing Co.");

        List<SavedItem> rows = new ArrayList<>();
        seed(rows, savedItemRepository, "a3000000-0000-0000-0000-000000000001",
                () -> savedItem(customerOne, EntityType.PROPERTY, "44444444-4444-4444-4444-444444444444"));
        seed(rows, savedItemRepository, "a3000000-0000-0000-0000-000000000002",
                () -> savedItem(customerOne, EntityType.COMPANY, "33333333-3333-3333-3333-333333333333"));
        seed(rows, savedItemRepository, "a3000000-0000-0000-0000-000000000003",
                () -> savedItem(customerTwo, EntityType.PROPERTY, "90000000-0000-0000-0000-000000000001"));
        seed(rows, savedItemRepository, "a3000000-0000-0000-0000-000000000004",
                () -> savedItem(customerTwo, EntityType.COMPANY, "80000000-0000-0000-0000-000000000001"));
        seed(rows, savedItemRepository, "a3000000-0000-0000-0000-000000000005",
                () -> savedItem(customerOne, EntityType.PROPERTY, "90000000-0000-0000-0000-000000000005"));
    }

    // ------------------------------------------------------------------
    // Moderation & admin
    // ------------------------------------------------------------------

    private void seedReports(List<User> users) {
        User customerOne = userByRole(users, "customer1@example.com");
        User customerTwo = userByRole(users, "customer2@example.com");
        User ownerOne = userByRole(users, "owner1@example.com");
        User admin = admin(users);

        List<Report> rows = new ArrayList<>();
        seed(rows, reportRepository, "b7000000-0000-0000-0000-000000000001",
                () -> report(customerOne, EntityType.PROPERTY, "90000000-0000-0000-0000-000000000003",
                        "Suspicious pricing, likely a spam listing.", ReportStatus.OPEN, null, null));
        seed(rows, reportRepository, "b7000000-0000-0000-0000-000000000002",
                () -> report(customerTwo, EntityType.COMPANY, "80000000-0000-0000-0000-000000000002",
                        "Fake before/after photos in the portfolio.", ReportStatus.REVIEWING, admin, null));
        seed(rows, reportRepository, "b7000000-0000-0000-0000-000000000003",
                () -> report(customerOne, EntityType.PROPERTY, "90000000-0000-0000-0000-000000000004",
                        "Duplicate of another active listing.", ReportStatus.RESOLVED, admin,
                        "Confirmed duplicate, removed the listing."));
        seed(rows, reportRepository, "b7000000-0000-0000-0000-000000000004",
                () -> report(customerTwo, EntityType.COMPANY, "80000000-0000-0000-0000-000000000003",
                        "Asks for advance payment before starting work.", ReportStatus.OPEN, null, null));
        seed(rows, reportRepository, "b7000000-0000-0000-0000-000000000005",
                () -> report(ownerOne, EntityType.PROPERTY, "44444444-4444-4444-4444-444444444444",
                        "Offensive photo used in the listing.", ReportStatus.DISMISSED, admin,
                        "Photo does not violate guidelines."));
    }

    private void seedFraudFlags(List<Property> properties, List<Company> companies) {
        List<FraudFlag> rows = new ArrayList<>();
        seed(rows, fraudFlagRepository, "b8000000-0000-0000-0000-000000000001",
                () -> fraudFlag(EntityType.PROPERTY, "90000000-0000-0000-0000-000000000003",
                        FraudFlagType.DUPLICATE_LISTING, "OPEN"));
        seed(rows, fraudFlagRepository, "b8000000-0000-0000-0000-000000000002",
                () -> fraudFlag(EntityType.COMPANY, "80000000-0000-0000-0000-000000000002",
                        FraudFlagType.FAKE_REVIEW, "REVIEWING"));
        seed(rows, fraudFlagRepository, "b8000000-0000-0000-0000-000000000003",
                () -> fraudFlag(EntityType.COMPANY, "80000000-0000-0000-0000-000000000003",
                        FraudFlagType.SPAM_MESSAGE, "OPEN"));
        seed(rows, fraudFlagRepository, "b8000000-0000-0000-0000-000000000004",
                () -> fraudFlag(EntityType.PROPERTY, "44444444-4444-4444-4444-444444444444",
                        FraudFlagType.DUPLICATE_LISTING, "RESOLVED"));
        seed(rows, fraudFlagRepository, "b8000000-0000-0000-0000-000000000005",
                () -> fraudFlag(EntityType.TECHNICIAN, "94000000-0000-0000-0000-000000000002",
                        FraudFlagType.ABNORMAL_FOLLOW_ACTIVITY, "OPEN"));
    }

    private void seedFeatureFlags() {
        List<FeatureFlag> rows = new ArrayList<>();
        seed(rows, featureFlagRepository, "b9000000-0000-0000-0000-000000000001",
                () -> featureFlag("smart_search", true, "{\"pct\":100}"));
        seed(rows, featureFlagRepository, "b9000000-0000-0000-0000-000000000002",
                () -> featureFlag("otp_sms", true, "{\"pct\":100}"));
        seed(rows, featureFlagRepository, "b9000000-0000-0000-0000-000000000003",
                () -> featureFlag("meilisearch_indexing", true, "{\"pct\":100}"));
        seed(rows, featureFlagRepository, "b9000000-0000-0000-0000-000000000004",
                () -> featureFlag("firebase_push", true, "{\"pct\":50}"));
        seed(rows, featureFlagRepository, "b9000000-0000-0000-0000-000000000005",
                () -> featureFlag("video_tours", false, "{\"pct\":0}"));
    }

    private void seedCmsPages() {
        List<CmsPage> rows = new ArrayList<>();
        seed(rows, cmsPageRepository, "c1000000-0000-0000-0000-000000000001",
                () -> cmsPage("about-us", "About Delta Homes",
                        "<p>Delta Homes is Egypt's marketplace for property and finishing services in the Nile Delta region.</p>", "en"));
        seed(rows, cmsPageRepository, "c1000000-0000-0000-0000-000000000002",
                () -> cmsPage("privacy-policy", "Privacy Policy",
                        "<p>We respect your privacy and protect your personal data.</p>", "en"));
        seed(rows, cmsPageRepository, "c1000000-0000-0000-0000-000000000003",
                () -> cmsPage("terms", "Terms of Service",
                        "<p>By using Delta Homes you agree to these terms.</p>", "en"));
        seed(rows, cmsPageRepository, "c1000000-0000-0000-0000-000000000004",
                () -> cmsPage("contact-us", "Contact Us",
                        "<p>Reach out at support@deltahomes.app or call 01026962089.</p>", "en"));
        seed(rows, cmsPageRepository, "c1000000-0000-0000-0000-000000000005",
                () -> cmsPage("help-center", "Help Center",
                        "<p>Find answers about listing, verifying and renting properties.</p>", "en"));
    }

    private void seedAuditLogs(List<User> users) {
        User admin = admin(users);
        List<AuditLog> rows = new ArrayList<>();
        seed(rows, auditLogRepository, "c2000000-0000-0000-0000-000000000001",
                () -> auditLog(admin, "USER_VERIFIED", "USER", "a1000000-0000-0000-0000-000000000001",
                        "197.58.120.11", "Approved commercial registry document"));
        seed(rows, auditLogRepository, "c2000000-0000-0000-0000-000000000002",
                () -> auditLog(admin, "COMPANY_VERIFIED", "COMPANY", "80000000-0000-0000-0000-000000000001",
                        "197.58.120.11", "Verified company documents"));
        seed(rows, auditLogRepository, "c2000000-0000-0000-0000-000000000003",
                () -> auditLog(admin, "PROPERTY_HIDDEN", "PROPERTY", "90000000-0000-0000-0000-000000000004",
                        "197.58.120.12", "Duplicate listing reported by user"));
        seed(rows, auditLogRepository, "c2000000-0000-0000-0000-000000000004",
                () -> auditLog(admin, "REPORT_RESOLVED", "REPORT", "b7000000-0000-0000-0000-000000000003",
                        "197.58.120.12", "Duplicate confirmed, listing removed"));
        seed(rows, auditLogRepository, "c2000000-0000-0000-0000-000000000005",
                () -> auditLog(admin, "COUPON_CREATED", "COUPON", "b6000000-0000-0000-0000-000000000001",
                        "197.58.120.13", "Created WELCOME10 coupon"));
    }

    // ------------------------------------------------------------------
    // Builders
    // ------------------------------------------------------------------

    private static City city(String name, String nameAr) {
        City city = new City();
        city.setName(name);
        city.setNameAr(nameAr);
        city.setIsActive(true);
        return city;
    }

    private static District district(City city, String name, String nameAr) {
        District district = new District();
        district.setCity(city);
        district.setName(name);
        district.setNameAr(nameAr);
        return district;
    }

    private static Feature feature(String name, String nameAr, String dataType) {
        Feature feature = new Feature();
        feature.setName(name);
        feature.setNameAr(nameAr);
        feature.setDataType(dataType);
        return feature;
    }

    private static Service service(String name, String nameAr, String category) {
        Service service = new Service();
        service.setName(name);
        service.setNameAr(nameAr);
        service.setCategory(category);
        return service;
    }

    private static SubscriptionPlan plan(String name, SubscriptionTier tier, String price,
                                         int listingCap, int broadcastCap) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName(name);
        plan.setTier(tier);
        plan.setPrice(new BigDecimal(price));
        plan.setListingCap(listingCap);
        plan.setBroadcastCap(broadcastCap);
        plan.setIsActive(true);
        return plan;
    }

    private static AdminRole adminRole(String name) {
        AdminRole role = new AdminRole();
        role.setName(name);
        return role;
    }

    private User ensureUser(String name, String phone, String email, UserRole role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setName(name);
            user.setPhone(phone);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode("secret123"));
            user.setRole(role);
            user.setStatus(UserStatus.ACTIVE);
            user.setVerificationLevel((byte) 1);
            return userRepository.save(user);
        });
    }

    private static Company company(User owner, CompanyType type, String name, String phone,
                                   String whatsapp, String email, double reputation) {
        Company company = new Company();
        company.setOwner(owner);
        company.setType(type);
        company.setName(name);
        company.setLogoUrl("https://picsum.photos/seed/" + name.replaceAll("\\s+", "").toLowerCase() + "/200/200");
        company.setCoverUrl("https://picsum.photos/seed/" + name.replaceAll("\\s+", "").toLowerCase() + "cover/1200/400");
        company.setDescription(name + " provides trusted " + type.name().toLowerCase().replace('_', ' ')
                + " services across the Nile Delta region.");
        company.setPhone(phone);
        company.setWhatsapp(whatsapp);
        company.setEmail(email);
        company.setWebsite("https://www." + email.substring(email.indexOf('@') + 1));
        company.setVerified(true);
        company.setFollowersCount(0);
        company.setReputationScore(BigDecimal.valueOf(reputation));
        company.setPlan(SubscriptionTier.PREMIUM);
        company.setCoverageArea("[\"Mansoura\",\"New Damietta\",\"Damietta\",\"Talkha\",\"Ras El Bar\"]");
        return company;
    }

    private static CompanyService cs(Company company, Service service) {
        CompanyService cs = new CompanyService();
        cs.setCompany(company);
        cs.setService(service);
        return cs;
    }

    private static CompanyStaff staff(Company company, User user, String role) {
        CompanyStaff staff = new CompanyStaff();
        staff.setCompany(company);
        staff.setUser(user);
        staff.setRole(role);
        return staff;
    }

    private static CompanyPortfolio portfolio(Company company, String caption, LocalDate date) {
        CompanyPortfolio portfolio = new CompanyPortfolio();
        portfolio.setCompany(company);
        portfolio.setBeforeUrl("https://picsum.photos/seed/" + company.getName().replaceAll("\\s+", "")
                + "before/800/600");
        portfolio.setAfterUrl("https://picsum.photos/seed/" + company.getName().replaceAll("\\s+", "")
                + "after/800/600");
        portfolio.setCaption(caption);
        portfolio.setProjectDate(date);
        return portfolio;
    }

    private static Property property(User owner, String title, String price, PropertyPurpose purpose,
                                     String category, City city, District district, String lat,
                                     String lng, FinishingLevel level, boolean featured) {
        Property property = new Property();
        property.setOwner(owner);
        property.setTitle(title);
        property.setDescription(title + " in " + city.getName() + ". "
                + "A well-located unit with excellent finishing, ready for immediate use. "
                + "Close to services, schools and public transportation.");
        property.setPrice(new BigDecimal(price));
        property.setPurpose(purpose);
        property.setCategory(category);
        property.setCity(city);
        property.setDistrict(district);
        property.setStreet("El-Gomhoria St");
        property.setLatitude(new BigDecimal(lat));
        property.setLongitude(new BigDecimal(lng));
        property.setStatus(PropertyStatus.PUBLISHED);
        property.setIsFeatured(featured);
        property.setFinishingLevel(level);
        property.setReadiness(Readiness.READY);
        property.setFeatures("[\"Elevator\",\"Parking\",\"Air Conditioning\"]");
        return property;
    }

    private static PropertyImage image(Property property, String url, int sortOrder) {
        PropertyImage image = new PropertyImage();
        image.setProperty(property);
        image.setUrl(url);
        image.setSortOrder(sortOrder);
        return image;
    }

    private static PropertyVideo video(Property property, String videoUrl, String thumb, int duration) {
        PropertyVideo video = new PropertyVideo();
        video.setProperty(property);
        video.setVideoUrl(videoUrl);
        video.setThumbnailUrl(thumb);
        video.setDurationSeconds(duration);
        return video;
    }

    private static Verification verification(User user, VerificationType type, VerificationStatus status,
                                             User reviewedBy) {
        Verification verification = new Verification();
        verification.setUser(user);
        verification.setType(type);
        verification.setDocumentUrl("https://picsum.photos/seed/doc-" + user.getEmail().substring(0, user.getEmail().indexOf('@')) + "/600/800");
        verification.setStatus(status);
        verification.setReviewedBy(reviewedBy);
        verification.setReviewedAt(status == VerificationStatus.ACCEPTED ? LocalDateTime.now().minusDays(3) : null);
        return verification;
    }

    private static AdminRoleUser roleUser(AdminRole role, User user) {
        AdminRoleUser roleUser = new AdminRoleUser();
        roleUser.setAdminRole(role);
        roleUser.setUser(user);
        return roleUser;
    }

    private static Conversation conversation(User userOne, User userTwo, String preview) {
        Conversation conversation = new Conversation();
        conversation.setUserOne(userOne);
        conversation.setUserTwo(userTwo);
        conversation.setLastMessagePreview(preview);
        return conversation;
    }

    private static Message message(Conversation conversation, User sender, String text) {
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setType(MessageType.TEXT);
        message.setTextBody(text);
        return message;
    }

    private static Notification notification(User user, String title, String body, NotificationType type,
                                             EntityType entityType, String entityId, boolean read) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setType(type);
        notification.setEntityType(entityType);
        notification.setEntityId(UUID.fromString(entityId));
        notification.setIsRead(read);
        return notification;
    }

    private static Appointment appointment(Property property, User customer, User owner,
                                           LocalDateTime slot, AppointmentStatus status, String note) {
        Appointment appointment = new Appointment();
        appointment.setProperty(property);
        appointment.setCustomer(customer);
        appointment.setOwner(owner);
        appointment.setRequestedSlot(slot);
        appointment.setStatus(status);
        appointment.setNote(note);
        return appointment;
    }

    private static Review review(User reviewer, EntityType entityType, String entityId, Byte rating,
                                 String comment, boolean verified, UUID sourceAppointmentId) {
        Review review = new Review();
        review.setReviewer(reviewer);
        review.setEntityType(entityType);
        review.setEntityId(UUID.fromString(entityId));
        review.setRating(rating);
        review.setComment(comment);
        review.setInteractionVerified(verified);
        review.setSourceAppointmentId(sourceAppointmentId);
        return review;
    }

    private static Broadcast broadcast(Company company, String title, String body, BroadcastType type) {
        Broadcast broadcast = new Broadcast();
        broadcast.setCompany(company);
        broadcast.setTitle(title);
        broadcast.setBody(body);
        broadcast.setType(type);
        return broadcast;
    }

    private static BroadcastDelivery delivery(Broadcast broadcast, User user, boolean opened,
                                              boolean clicked, LocalDateTime openedAt) {
        BroadcastDelivery delivery = new BroadcastDelivery();
        delivery.setBroadcast(broadcast);
        delivery.setUser(user);
        delivery.setOpened(opened);
        delivery.setClicked(clicked);
        delivery.setOpenedAt(openedAt);
        return delivery;
    }

    private static FollowerPreference preference(User user, Company company, boolean offers, boolean videos,
                                                 boolean newProps, boolean news, boolean discounts) {
        FollowerPreference preference = new FollowerPreference();
        preference.setUser(user);
        preference.setCompany(company);
        preference.setWantsOffers(offers);
        preference.setWantsVideos(videos);
        preference.setWantsNewProperties(newProps);
        preference.setWantsNews(news);
        preference.setWantsDiscounts(discounts);
        return preference;
    }

    private static Follower follower(User user, Company company) {
        Follower follower = new Follower();
        follower.setUser(user);
        follower.setCompany(company);
        return follower;
    }

    private static Subscription subscription(UUID userId, UUID companyId, SubscriptionPlan plan,
                                             LocalDate start, LocalDate end, SubscriptionStatus status) {
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setCompanyId(companyId);
        subscription.setPlan(plan);
        subscription.setStartDate(start);
        subscription.setEndDate(end);
        subscription.setStatus(status);
        return subscription;
    }

    private static Payment payment(Subscription subscription, BigDecimal amount, String method,
                                   PaymentStatus status, String gatewayReference) {
        Payment payment = new Payment();
        payment.setSubscription(subscription);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setStatus(status);
        payment.setGatewayReference(gatewayReference);
        return payment;
    }

    private static Coupon coupon(String code, byte discountPercent, LocalDate from, LocalDate to, int maxUses) {
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setDiscountPercent(discountPercent);
        coupon.setValidFrom(from);
        coupon.setValidTo(to);
        coupon.setMaxUses(maxUses);
        return coupon;
    }

    private static SavedItem savedItem(User user, EntityType entityType, String entityId) {
        SavedItem savedItem = new SavedItem();
        savedItem.setUser(user);
        savedItem.setEntityType(entityType);
        savedItem.setEntityId(UUID.fromString(entityId));
        return savedItem;
    }

    private static Report report(User reporter, EntityType entityType, String entityId, String reason,
                                 ReportStatus status, User assignedStaff, String decision) {
        Report report = new Report();
        report.setReporter(reporter);
        report.setEntityType(entityType);
        report.setEntityId(UUID.fromString(entityId));
        report.setReason(reason);
        report.setStatus(status);
        report.setAssignedStaff(assignedStaff);
        report.setDecision(decision);
        return report;
    }

    private static FraudFlag fraudFlag(EntityType entityType, String entityId, FraudFlagType flagType,
                                       String status) {
        FraudFlag flag = new FraudFlag();
        flag.setEntityType(entityType);
        flag.setEntityId(UUID.fromString(entityId));
        flag.setFlagType(flagType);
        flag.setStatus(status);
        return flag;
    }

    private static FeatureFlag featureFlag(String key, boolean enabled, String scope) {
        FeatureFlag flag = new FeatureFlag();
        flag.setKey(key);
        flag.setIsEnabled(enabled);
        flag.setRolloutScope(scope);
        return flag;
    }

    private static CmsPage cmsPage(String slug, String title, String bodyHtml, String locale) {
        CmsPage page = new CmsPage();
        page.setSlug(slug);
        page.setTitle(title);
        page.setBodyHtml(bodyHtml);
        page.setLocale(locale);
        return page;
    }

    private static AuditLog auditLog(User admin, String action, String targetType, String targetId,
                                     String ip, String reason) {
        AuditLog log = new AuditLog();
        log.setAdmin(admin);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(UUID.fromString(targetId));
        log.setIpAddress(ip);
        log.setReason(reason);
        return log;
    }

    // ------------------------------------------------------------------
    // Reference lookups
    // ------------------------------------------------------------------

    private static City city(List<City> cities, String name) {
        return cities.stream().filter(c -> c.getName().equals(name)).findFirst().orElseThrow();
    }

    private static District district(List<District> districts, String name) {
        return districts.stream().filter(d -> d.getName().equals(name)).findFirst().orElseThrow();
    }

    private static Service service(List<Service> services, String name) {
        return services.stream().filter(s -> s.getName().equals(name)).findFirst().orElseThrow();
    }

    private static SubscriptionPlan plan(List<SubscriptionPlan> plans, String name) {
        return plans.stream().filter(p -> p.getName().equals(name)).findFirst().orElseThrow();
    }

    private static Company company(List<Company> companies, String name) {
        return companies.stream().filter(c -> c.getName().equals(name)).findFirst().orElseThrow();
    }

    private static Property property(List<Property> properties, String title) {
        return properties.stream().filter(p -> p.getTitle().equals(title)).findFirst().orElseThrow();
    }

    private static User userByRole(List<User> users, String email) {
        return users.stream().filter(u -> email.equals(u.getEmail())).findFirst().orElseThrow();
    }

    private static User admin(List<User> users) {
        return users.stream().filter(u -> u.getRole() == UserRole.ADMIN).findFirst().orElseThrow();
    }

    // ------------------------------------------------------------------
    // Insert helper
    // ------------------------------------------------------------------

    private static <T> void seed(List<T> collected, JpaRepository<T, UUID> repo, String uuid, Supplier<T> factory) {
        UUID id = UUID.fromString(uuid);
        if (repo.existsById(id)) {
            repo.findById(id).ifPresent(collected::add);
            return;
        }
        T entity = factory.get();
        setId(entity, id);
        // save() → merge() path: with no @GeneratedValue on the id, Hibernate
        // honors the pre-assigned id (the old generator used to replace it).
        collected.add(repo.save(entity));
    }

    private static void setId(Object entity, UUID id) {
        try {
            var method = entity.getClass().getMethod("setId", UUID.class);
            method.invoke(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot set id on " + entity.getClass().getSimpleName(), e);
        }
    }
}
