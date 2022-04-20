/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.models.map.storage.jpa.realm;

import java.util.function.BiFunction;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.keycloak.models.RealmModel;
import org.keycloak.models.map.storage.CriterionNotSupportedException;
import org.keycloak.models.map.storage.jpa.JpaModelCriteriaBuilder;
import org.keycloak.models.map.storage.jpa.hibernate.jsonb.JsonbType;
import org.keycloak.models.map.storage.jpa.realm.entity.JpaRealmEntity;
import org.keycloak.storage.SearchableModelField;


/**
 * A {@link JpaModelCriteriaBuilder} implementation for realms.
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class JpaRealmModelCriteriaBuilder extends JpaModelCriteriaBuilder<JpaRealmEntity, RealmModel, JpaRealmModelCriteriaBuilder> {

    public JpaRealmModelCriteriaBuilder() {
        super(JpaRealmModelCriteriaBuilder::new);
    }

    private JpaRealmModelCriteriaBuilder(final BiFunction<CriteriaBuilder, Root<JpaRealmEntity>, Predicate> predicateFunc) {
        super(JpaRealmModelCriteriaBuilder::new, predicateFunc);
    }

    private JpaRealmModelCriteriaBuilder(final BiFunction<CriteriaBuilder, Root<JpaRealmEntity>, Predicate> predicateFUnc,
                                         final boolean isDistinct) {
        super(JpaRealmModelCriteriaBuilder::new, predicateFUnc, isDistinct);
    }

    @Override
    public JpaRealmModelCriteriaBuilder compare(SearchableModelField<? super RealmModel> modelField, Operator op, Object... value) {
        switch(op) {
            case EQ:
                if (modelField.equals(RealmModel.SearchableFields.NAME)) {
                    validateValue(value, modelField, op, String.class);
                    return new JpaRealmModelCriteriaBuilder((cb, root) ->
                            cb.equal(root.get(modelField.getName()), value[0])
                    );
                } else if (modelField.equals(RealmModel.SearchableFields.COMPONENT_PROVIDER_TYPE)) {
                    validateValue(value, modelField, op, String.class);
                    return new JpaRealmModelCriteriaBuilder((cb, root) ->
                            cb.equal(root.join("components").get("providerType"), value[0]), true);
                } else {
                    throw new CriterionNotSupportedException(modelField, op);
                }
            case EXISTS:
                if (modelField.equals(RealmModel.SearchableFields.CLIENT_INITIAL_ACCESS)) {
                    return new JpaRealmModelCriteriaBuilder((cb, root) ->
                        cb.isTrue(cb.function("->", JsonbType.class, root.get("metadata"),
                                cb.literal("fClientInitialAccesses")).isNotNull())
                    );
                } else {
                    throw new CriterionNotSupportedException(modelField, op);
                }
            default:
                throw new CriterionNotSupportedException(modelField, op);
        }
    }
}
