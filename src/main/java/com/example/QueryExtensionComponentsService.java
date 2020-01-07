package com.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jbpm.services.api.query.QueryService;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.jbpm.JbpmKieServerExtension;

public class QueryExtensionComponentsService implements KieServerApplicationComponentsService {
    private static final String OWNER_EXTENSION = JbpmKieServerExtension.EXTENSION_NAME;

    @Override
    public Collection<Object> getAppComponents(String extension, SupportedTransports supportedTransports,
            Object... services) {

        if (!OWNER_EXTENSION.equals(extension)) {
            return Collections.emptyList();
        }

        KieServerRegistry kieServerRegistry = null;
        QueryService queryService = null;

        for (Object object : services) {
            if (QueryService.class.isAssignableFrom(object.getClass())) {
                queryService = (QueryService) object;
                continue;
            } else if (KieServerRegistry.class.isAssignableFrom(object.getClass())) {
                kieServerRegistry = (KieServerRegistry) object;
                continue;
            }
        }

        List<Object> components = new ArrayList<Object>(1);
        if (SupportedTransports.REST.equals(supportedTransports)) {
            components.add(new VariablesQuery(kieServerRegistry, queryService));
        }
        
        System.out.println("QueryExtensionComponentsService.getAppComponents()");
        return components;
    }
}