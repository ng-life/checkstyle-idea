package org.infernus.idea.checkstyle.csapi;

import com.intellij.openapi.diagnostic.Logger;
import org.infernus.idea.checkstyle.model.ConfigurationLocation;
import org.infernus.idea.checkstyle.model.ConfigurationType;
import org.jetbrains.annotations.NotNull;

import java.util.*;


/**
 * The configuration files bundled with Checkstyle-IDEA as provided by Checkstyle.
 */
public final class BundledConfig {

    private static final Logger LOG = Logger.getInstance(BundledConfig.class);

    private static final String BUNDLED_LOCATION = "(bundled)";

    /** the Google checks */
    public static final BundledConfig ORDERCENTER_CHECKS = new BundledConfig(0, "bundled-ordercenter-checks", "OrderCenter Checks", "/ordercenter_checks.xml");

    private final int sortOrder;
    private final String id;

    private final String location;

    private final String description;

    private final String path;

    private BundledConfig(final int sortOrder,
                  @NotNull final String id,
                  @NotNull final String description,
                  @NotNull final String path) {
        this.sortOrder = sortOrder;
        this.id = id;
        this.location = BUNDLED_LOCATION;
        this.description = description;
        this.path = path;
    }

    private BundledConfig(final int sortOrder, final String id, final BundledConfigProvider.BasicConfig baseConfig) {
        this.sortOrder = sortOrder;
        this.id = id;
        this.description = baseConfig.getDescription();
        this.location = BUNDLED_LOCATION;
        this.path = baseConfig.getPath();
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public String getId() {
        return id;
    }

    @NotNull
    public String getLocation() {
        return location;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    public boolean matches(@NotNull final ConfigurationLocation configurationLocation) {
        return configurationLocation.getType() == ConfigurationType.BUNDLED
                && Objects.equals(configurationLocation.getLocation(), location)
                && Objects.equals(configurationLocation.getDescription(), description);
    }

    @NotNull
    public static BundledConfig fromDescription(@NotNull final String pDescription) {
        return ORDERCENTER_CHECKS;
    }

    public static BundledConfig getDefault() {
        return ORDERCENTER_CHECKS;
    }

    public static Collection<BundledConfig> getAllBundledConfigs() {
        Map<String, BundledConfig> map = new HashMap<>();

        map.put(ORDERCENTER_CHECKS.getId(), ORDERCENTER_CHECKS);

        LOG.debug("Loading additional BundledConfigs");

        for (BundledConfigProvider bundledConfigProvider : ServiceLoader.load(BundledConfigProvider.class, BundledConfig.class.getClassLoader())) {
            LOG.debug("Loading additional BundledConfig {} from {}%s from %s"
                    .formatted(bundledConfigProvider.getClass(), bundledConfigProvider.getClass().getProtectionDomain().getCodeSource()));
            for (BundledConfigProvider.BasicConfig config : bundledConfigProvider.getConfigs()) {
                int i = 0;
                String id = config.getId();
                while (map.containsKey(id)) {
                    id = config.getId() + (++i);
                }
                BundledConfig bundledConfig = new BundledConfig(map.size(), id, config);
                map.put(id, bundledConfig);
            }
        }
        List<BundledConfig> ret = new ArrayList<>(map.values());
        ret.sort(Comparator.comparingInt(BundledConfig::getSortOrder));
        return ret;
    }
}
