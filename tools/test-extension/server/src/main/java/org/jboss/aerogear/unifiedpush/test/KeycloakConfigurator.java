/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.test;

import org.keycloak.models.jpa.entities.*;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class which handles configuration of the keycloak so its testable.
 */
@Stateless
public class KeycloakConfigurator {
    private static final Logger LOGGER = Logger.getLogger(KeycloakConfigurator.class.getName());

    @PersistenceContext(unitName = "keycloak-default")
    EntityManager entityManager;

    /**
     * Changes keycloak configuration - removes all required actions for all users in the aerogear realm.
     */
    public KeycloakConfigurationResult configureForIntegrationTests() {

        KeycloakConfigurationResult result = new KeycloakConfigurationResult();

        TypedQuery<RealmEntity> realmQuery = entityManager.createNamedQuery("getRealmByName", RealmEntity.class);
        realmQuery.setParameter("name", "aerogear");

        for (RealmEntity realm : realmQuery.getResultList()) {
            LOGGER.warning("Editing realm: " + realm.getName());
            String realmReadableId = realm.getName() + ":" + realm.getId();
            result.getFoundRealms().add(realmReadableId);

            // Make sure the session won't expire even when the testing runs very slow
            result.getExtra().put(realmReadableId + "-accessTokenLifespan",
                    realm.getAccessTokenLifespan());
            realm.setAccessTokenLifespan(3600);

            result.getExtra().put(realmReadableId + "-accessCodeLifespan",
                    realm.getAccessCodeLifespan());
            realm.setAccessCodeLifespan(3600);

            result.getExtra().put(realmReadableId + "-accessCodeLifespanUserAction",
                    realm.getAccessCodeLifespanUserAction());
            realm.setAccessCodeLifespanUserAction(3600);

            result.getExtra().put(realmReadableId + "-ssoSessionIdleTimeout",
                    realm.getSsoSessionIdleTimeout());
            realm.setSsoSessionIdleTimeout(3600);

            entityManager.merge(realm);

            TypedQuery<UserEntity> userQuery = entityManager.createNamedQuery("getAllUsersByRealm", UserEntity.class);
            userQuery.setParameter("realmId", realm.getId());

            // Any required action would prevent us to login
            for (UserEntity user : userQuery.getResultList()) {
                LOGGER.log(Level.INFO, "Editing user: {0}", user.getUsername());
                result.getFoundUsers().add(user.getUsername());
                for (UserRequiredActionEntity userRequiredAction : user.getRequiredActions()) {
                    LOGGER.log(Level.INFO, "Removing required action: {0}", userRequiredAction.getAction());
                    String current = result.getRemovedRequiredActions().get(user.getUsername());
                    result.getRemovedRequiredActions().put(user.getUsername(),
                        (current == null || current.equals("null") ? "" : current + ", ") + userRequiredAction.getAction() + ", ");
                    entityManager.remove(userRequiredAction);
                }
                user.getRequiredActions().clear();
            }
        }

        return result;
    }

    public List<String> getRealms() {
        List<String> result = new ArrayList<String>();

        TypedQuery<RealmEntity> realmQuery = entityManager.createNamedQuery("getAllRealms", RealmEntity.class);

        for (RealmEntity realm : realmQuery.getResultList()) {
            result.add(realm.getName());
        }

        return result;
    }
}
