/*******************************************************************************
* Copyright (c) 2022 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package io.openliberty.tools.langserver.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.openliberty.tools.langserver.ls.LibertyTextDocument;
import io.openliberty.tools.langserver.model.propertiesfile.PropertiesEntryInstance;

/**
 * Maps property keys with their list of valid values for server.env and bootstrap.properties
 */
public class ServerPropertyValues {
    private final static List<String> LOGGING_SOURCE_VALUES = Arrays.asList("message", "trace", "accessLog", "ffdc", "audit");
    private final static List<String> BOOLEAN_VALUES_DEFAULT_TRUE = Arrays.asList("true", "false");
    private final static List<String> BOOLEAN_VALUES_DEFAULT_FALSE = Arrays.asList("false", "true");
    private final static List<String> YES_NO_VALUES = Arrays.asList("y", "n");
    
    private static HashMap<String, List<String>> validServerValues = new HashMap<String, List<String>>() {{
        put("WLP_DEBUG_SUSPEND", YES_NO_VALUES); // default yes
        put("WLP_DEBUG_REMOTE", YES_NO_VALUES); // default undefined/disabled, but will preselect "y" to enable
        
        put("WLP_LOGGING_CONSOLE_FORMAT", Arrays.asList("DEV", "SIMPLE", "JSON", "TBASIC")); // default "DEV"
        put("WLP_LOGGING_CONSOLE_LOGLEVEL", Arrays.asList("AUDIT", "INFO", "WARNING", "ERROR", "OFF")); // default "AUDIT"
        put("WLP_LOGGING_CONSOLE_SOURCE", LOGGING_SOURCE_VALUES); //default "message"
        put("WLP_LOGGING_MESSAGE_FORMAT", Arrays.asList("SIMPLE", "JSON", "TBASIC")); // default "SIMPLE"
        put("WLP_LOGGING_MESSAGE_SOURCE", LOGGING_SOURCE_VALUES); //default "message"
        put("WLP_LOGGING_APPS_WRITE_JSON", BOOLEAN_VALUES_DEFAULT_FALSE); //default false
    }};

    private static HashMap<String, List<String>> validBootstrapValues = new HashMap<String, List<String>>() {{
        put("com.ibm.ws.logging.copy.system.streams", BOOLEAN_VALUES_DEFAULT_TRUE); // default true
        put("com.ibm.ws.logging.newLogsOnStart", BOOLEAN_VALUES_DEFAULT_TRUE); // default true
        put("com.ibm.ws.logging.isoDateFormat", BOOLEAN_VALUES_DEFAULT_FALSE); // default false
        put("com.ibm.ws.logging.trace.format", Arrays.asList("ENHANCED", "BASIC", "TBASIC", "ADVANCED")); // default "ENHANCED"
        put("websphere.log.provider", Arrays.asList("binaryLogging-1.0"));
        put("com.ibm.hpel.log.bufferingEnabled", BOOLEAN_VALUES_DEFAULT_TRUE); // default true
        put("com.ibm.hpel.trace.bufferingEnabled", BOOLEAN_VALUES_DEFAULT_TRUE); // default true
        EquivalentProperties.getBootstrapKeys().forEach(
            bskey -> {
                String serverEnvEquivalent = EquivalentProperties.getEquivalentProperty(bskey);
                if(validServerValues.containsKey(serverEnvEquivalent)) {
                    this.put(bskey, validServerValues.get(serverEnvEquivalent));
                }
            }
        );
    }};

    private static Set<String> caseSensitiveProperties = new HashSet<String>() {{
        add("WLP_DEBUG_SUSPEND");
        add("WLP_DEBUG_REMOTE");
        add("WLP_LOGGING_CONSOLE_SOURCE");
        add("WLP_LOGGING_MESSAGE_SOURCE");
        EquivalentProperties.getServerVarKeys().forEach(
            serverKey -> {
                if(this.contains(serverKey)) {
                    this.add(EquivalentProperties.getEquivalentProperty(serverKey));
                }
            }
        );
    }};

    public static boolean canValidateKeyValues(LibertyTextDocument tdi, String key) {
        if (ParserFileHelperUtil.isBootstrapPropertiesFile(tdi)) {
            return validBootstrapValues.containsKey(key);
        } else if (ParserFileHelperUtil.isServerEnvFile(tdi)) {
            return validServerValues.containsKey(key);
        }
        return false;
    }

    public static List<String> getValidValues(LibertyTextDocument tdi, String key) {
        if (ParserFileHelperUtil.isBootstrapPropertiesFile(tdi)) {
            return validBootstrapValues.getOrDefault(key, Collections.emptyList());
        } else if (ParserFileHelperUtil.isServerEnvFile(tdi)) {
            return validServerValues.getOrDefault(key, Collections.emptyList());
        }
        return Collections.emptyList();
    }

    public static boolean isCaseSensitive(String key) {
        return caseSensitiveProperties.contains(key);
    }

    /**
     * Reads input line, parses key and value. Checks if value is valid for the given key.
     * @param line
     * @return True if key has valid value, otherwise false
     */
    public static PropertiesValidationResult validateServerProperty(String line, LibertyTextDocument textDocumentItem) {
        PropertiesEntryInstance entry = new PropertiesEntryInstance(line, textDocumentItem);
        PropertiesValidationResult result = new PropertiesValidationResult(entry);
        result.validateServerProperty();
        return result;
    }
}