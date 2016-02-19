package org.jahia.services.render.scripting.thymeleaf.expression.variables;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by smomin on 2/16/16.
 */
public class UserUtils {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(UserUtils.class);

    public UserUtils() {
        super();
    }

    public static Boolean memberOf(final String groups,
                                   final RenderContext renderContext) {
        final JahiaUser currentUser = JCRSessionFactory.getInstance().getCurrentUser();
        if (currentUser != null) {
            final JCRUserNode userNode = JahiaUserManagerService.getInstance()
                    .lookupUserByPath(currentUser.getLocalPath());
            if (userNode != null) {
                final String siteKey = retrieveSiteKey(renderContext);
                final String[] groupArray = StringUtils.split(groups, ',');
                for (final String aGroupArray : groupArray) {
                    final String groupName = aGroupArray.trim();
                    if (userNode.isMemberOfGroup(siteKey, groupName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Boolean notMemberOf(final String groups,
                                      final RenderContext renderContext) {
        final JahiaUser currentUser = JCRSessionFactory.getInstance().getCurrentUser();
        if (currentUser != null) {
            final JCRUserNode userNode = JahiaUserManagerService.getInstance()
                    .lookupUserByPath(currentUser.getLocalPath());
            if (userNode != null) {
                final String siteKey = retrieveSiteKey(renderContext);
                final String[] groupArray = StringUtils.split(groups, ',');
                for (final String aGroupArray : groupArray) {
                    final String groupName = aGroupArray.trim();
                    if (userNode.isMemberOfGroup(siteKey, groupName)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static Collection<JCRNodeWrapper> getMembers(final String group,
                                                        final RenderContext renderContext) {
        return JahiaGroupManagerService.getInstance().lookupGroupByPath(group).getMembers();
    }

    private static String retrieveSiteKey(final RenderContext renderContext) {
        String siteId = null;
        if (renderContext != null && renderContext.getSite() != null) {
            siteId = renderContext.getSite().getSiteKey();
        }
        return siteId;
    }

    /**
     * Looks up the user by the specified user key (user node path) or username.
     *
     * @param user the key or the name of the user to perform lookup for
     * @return the user for the specified user key or name or <code>null</code> if the corresponding user cannot be found
     * @throws IllegalArgumentException in case the specified user key is <code>null</code>
     */
    public static JCRUserNode lookupUser(final String user) throws IllegalArgumentException {
        if (user == null) {
            throw new IllegalArgumentException("Specified user key is null");
        }
        return ServicesRegistry.getInstance().getJahiaUserManagerService().lookup(user);
    }

    public static JCRUserNode lookupUser(final String user,
                                         final String site) throws IllegalArgumentException {
        if (user == null) {
            throw new IllegalArgumentException("Specified user key is null");
        }
        return ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(user, site);
    }

    public static Map<String, JCRGroupNode> getUserMembership(final String user) {
        return getUserMembership(lookupUser(user));
    }

    public static Map<String, JCRGroupNode> getUserMembership(final JCRNodeWrapper user) {
        final Map<String, JCRGroupNode> map = new LinkedHashMap<String, JCRGroupNode>();
        final JahiaGroupManagerService managerService = ServicesRegistry.getInstance()
                .getJahiaGroupManagerService();
        final List<String> userMembership = managerService.getMembershipByPath(user.getPath());
        for (final String groupPath : userMembership) {
            if(!groupPath.endsWith("/" + JahiaGroupManagerService.GUEST_GROUPNAME) &&
                    !groupPath.endsWith("/" + JahiaGroupManagerService.USERS_GROUPNAME) &&
                    !groupPath.endsWith("/" + JahiaGroupManagerService.SITE_USERS_GROUPNAME)) {
                final JCRGroupNode group = managerService.lookupGroupByPath(groupPath);
                map.put(groupPath,group);
            }
        }
        return map;
    }

    /**
     * Returns whether the current user can be assigned to the specified task (as represented by the specified JCRNodeWrapper).
     *
     * @param task a JCRNodeWrapper representing a WorkflowTask
     * @return <code>true</code> if the user can be assigned to the task, <code>false</code> otherwise
     * @throws RepositoryException
     */
    public static Boolean isAssignable(final JCRNodeWrapper task) throws RepositoryException {
        final JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        if (user == null || task == null) {
            return false;
        } else {
            if (task.hasProperty("candidates")) {
                final JahiaGroupManagerService managerService = ServicesRegistry.getInstance()
                        .getJahiaGroupManagerService();

                // candidates are using the path
                final String formattedUserName = user.getUserKey();

                // look at all the candidates for assignment
                final Value[] candidatesValues = task.getProperty("candidates").getValues();
                for (final Value value : candidatesValues) {
                    final String candidate = value.getString();

                    // first check if the current candidate is the user name
                    if (candidate.equals(formattedUserName)) {
                        // if it is, we're done
                        return true;
                    } else {
                        // otherwise, check if we're looking at a group, extract the group name and check whether the user is a member of
                        // that group
                        if (candidate.contains("/groups/")) {
                            final JCRGroupNode candidateGroup = managerService.lookupGroupByPath(candidate);
                            if (candidateGroup != null) {
                                if (candidateGroup.isMember(user.getLocalPath())) {
                                    return true;
                                }
                            } else {
                                LOGGER.info("Unable to lookup group for key {}."
                                        + " Skipping it when checking task assignee candidates.", candidate);
                            }
                        }
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the full user name, including first and last name. If those are
     * not available, returns the username.
     *
     * @param userNode the user JCR node
     * @return the full user name, including first and last name. If those are
     *         not available, returns the username
     */
    public static String userFullName(final JCRNodeWrapper userNode) {
        final StringBuilder name = new StringBuilder();
        String value = userNode.getPropertyAsString("j:firstName");
        if (StringUtils.isNotEmpty(value)) {
            name.append(value);
        }
        value = userNode.getPropertyAsString("j:lastName");
        if (StringUtils.isNotEmpty(value)) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(value);
        }

        if (name.length() == 0) {
            name.append(PrincipalViewHelper.getUserDisplayName(userNode.getName()));
        }

        return name.toString();
    }

    public static Set<JCRUserNode> searchUsers(final Map<String, String> criterias) {
        final Properties searchCriterias = new Properties();
        if (criterias == null || criterias.isEmpty()) {
            searchCriterias.setProperty("*", "*");
        } else {
            for (String key : criterias.keySet()) {
                searchCriterias.setProperty(key, criterias.get(key));
            }
        }

        final JahiaUserManagerService userManagerService = ServicesRegistry.getInstance()
                .getJahiaUserManagerService();
        final Set<JCRUserNode> searchResults = new HashSet<JCRUserNode>();
        searchResults.addAll(userManagerService.searchUsers(searchCriterias));
        return searchResults;
    }

    public static Boolean isPropertyEditable(final JCRUserNode userNode,
                                             final String name) {
        return userNode.isPropertyEditable(name);
    }

    public static String formatUserValueOption(final Object principal) {
        return new PrincipalViewHelper(new String[]{"Name,30","Properties,30"})
                .getPrincipalValueOption(principal);
    }

    public static String formatUserTextOption(final JCRNodeWrapper principal,
                                              final String fieldsToDisplay) {
        return new PrincipalViewHelper(fieldsToDisplay.split(";")).getPrincipalTextOption(principal);
    }

    public static Boolean isReadOnlyProvider(final JCRNodeWrapper principal) {
        return principal.getProvider().isReadOnly();
    }
}
