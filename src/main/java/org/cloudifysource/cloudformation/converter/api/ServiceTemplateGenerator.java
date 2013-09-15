/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/
package org.cloudifysource.cloudformation.converter.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.cloudifysource.cloudformation.converter.api.json.Lifecycle;
import org.cloudifysource.cloudformation.converter.api.json.ServiceDetails;
import org.cloudifysource.cloudformation.converter.api.json.filetype.ScriptFile2Generate;
import org.cloudifysource.cloudformation.converter.api.json.filetype.ScriptType;
import org.cloudifysource.cloudformation.converter.api.json.filetype.StringContent;

/**
 * Generates Cloudify service template from the DSL.
 * 
 * @author victor
 * 
 */
public class ServiceTemplateGenerator extends GroovyTemplateGenerator {

    /**
     * @author victor
     * 
     */
    private class ScriptFile {
        private final File originalFile;
        private final String destinationFilename;

        public ScriptFile(final File originalFile) {
            this.originalFile = originalFile;
            this.destinationFilename = originalFile.getName();
        }

        public ScriptFile(final File originalFile, final String suffix) {
            this.originalFile = originalFile;
            this.destinationFilename = suffix + originalFile.getName();
        }

        public String getDestinationFilename() {
            return destinationFilename;
        }

        public File getOriginalFile() {
            return originalFile;
        }
    }

    private String sourceAppDir;

    private ServiceDetails detail;

    private File serviceFolderDest;

    // List of generated external scripts
    private List<ScriptFile> externalFileToCopy = new ArrayList<ScriptFile>();

    public ServiceTemplateGenerator(final ServiceDetails detail, final String sourceAppDir,
            final File serviceFolderDest) {
        this.sourceAppDir = sourceAppDir;
        this.detail = detail;
        this.serviceFolderDest = serviceFolderDest;
    }

    /**
     * Generate service groovy files and copy external scripts into target folder if possible.
     * 
     * @param destinationFolder
     *            The destination folder where generate the scripts.
     * @return The generate file or folder.
     * @exception IOException
     *                If an error occurs with the file/folder creation.
     */
    public File generateFiles(final File destinationFolder) throws IOException {
        final File file = new File(serviceFolderDest, detail.getServiceName() + "-service.groovy");
        FileUtils.write(file, this.generate());

        // If sourceAppDir is null, we are working with templateBody templates so there is no external scripts.
        if (sourceAppDir != null) {
            // Copy existing external scripts
            final File serviceFolderSrc = new File(sourceAppDir, detail.getServiceName());
            if (serviceFolderSrc.isDirectory()) {
                // Copy service directory if exists.
                // If external scripts points to other external scripts, we have to copy the folder to keep them.
                FileUtils.copyDirectory(serviceFolderSrc, serviceFolderDest, true);
            }
        }

        // Copy generated external files into service destination folder.
        for (final ScriptFile scriptToCopy : externalFileToCopy) {
            FileUtils.copyFile(scriptToCopy.getOriginalFile(),
                    new File(serviceFolderDest, scriptToCopy.getDestinationFilename()), true);
        }

        // Clean unnecessary files
        if (detail.getMonitoring() != null) {
            this.deleteScriptFileIfExistsInDestinationFolder(detail.getMonitoring().getStartDetection());
            this.deleteScriptFileIfExistsInDestinationFolder(detail.getMonitoring().getPerformance());
        }
        this.deleteScriptFileIfExistsInDestinationFolder(detail.getScaling());

        return file;
    }

    private void deleteScriptFileIfExistsInDestinationFolder(final ScriptType scriptType) {
        if (scriptType != null && scriptType instanceof StringContent) {
            final File scriptFile = new File(scriptType.getValue());
            final File targetFile = new File(serviceFolderDest, scriptFile.getName());
            if (targetFile.exists()) {
                targetFile.delete();
            }
        }
    }

    @Override
    protected void doGenerate() throws IOException {
        openBrace("service");
        appendQuotedKeyValue("name", detail.getServiceName());
        appendUnquotedKeyValue("numInstances", detail.getNumInstance());
        appendUnquotedKeyValue("minAllowedInstances", "1");
        appendUnquotedKeyValue("maxAllowedInstances", "2048");
        appendUnquotedKeyValue("elastic", true);
        appendQuotedKeyValue("type", "APP_SERVER");
        appendLifeCycle();
        appendCustomCommands();
        appendMonitors();
        closeBrace();
    }

    private void appendMonitors() throws IOException {
        if (detail.getScaling() != null) {
            openArray("scalingRules");
            appendFileContent("scalingRule", detail.getScaling());
            closeArray();
        }
    }

    private void appendCustomCommands() {
        if (detail.getCustomCommands() != null && !detail.getCustomCommands().isEmpty()) {
            openArray("customCommands");
            for (final Entry<String, ScriptType> customCommand : detail.getCustomCommands().entrySet()) {
                appendCustomCommandFilePath(customCommand.getKey(), customCommand.getValue());
            }
            builder.setLength(builder.length() - 2);
            builder.append("\n");
            closeArray();
        }
    }

    private void appendCustomCommandFilePath(final String key, final ScriptType scriptType) {
        final ScriptFile script = this.getScriptFile(scriptType, key, true);
        appendSemiColonKeyValue(key, script.getDestinationFilename());

    }

    private void appendLifeCycle() throws IOException {
        openBrace("lifecycle");
        appendBracedKeyValue("locator", "NO_PROCESS_LOCATORS");
        if (detail.getMonitoring() != null) {
            if (detail.getMonitoring().getStartDetection() != null) {
                appendUnquotedKeyValue("startDetectionTimeoutSecs", "900");
                appendFileContent("startDetection", detail.getMonitoring().getStartDetection());
            }
            appendFileContent("monitors", detail.getMonitoring().getPerformance());
        }
        if (detail.getLifecycle() != null) {
            final Lifecycle lifecycle = detail.getLifecycle();
            appendExternalFile("postStart", lifecycle.getPostStart());
            appendExternalFile("preStop", lifecycle.getPreStop());
            appendExternalFile("stop", lifecycle.getStop());
            appendExternalFile("postStop", lifecycle.getPostStop());
        }
        closeBrace();
    }

    private void appendExternalFile(final String key, final ScriptType scriptType) throws IOException {
        if (scriptType != null) {
            final ScriptFile script = this.getScriptFile(scriptType, key, true);
            this.appendQuotedKeyValue(key, script.getDestinationFilename());
        }

    }

    private void appendFileContent(final String key, final ScriptType filepath) throws IOException {
        if (filepath != null) {
            final ScriptFile scriptFile = this.getScriptFile(filepath, key, false);
            final List<String> scriptString = FileUtils.readLines(scriptFile.getOriginalFile());
            appendBracedKeyValue(key, scriptString);
        }
    }

    private ScriptFile getScriptFile(final ScriptType script, final String suffixForGenerationScript,
            final boolean isScriptToCopy) {
        ScriptFile scriptFile = null;
        if (script instanceof StringContent) {
            final String parent = sourceAppDir + File.separator + detail.getServiceName();
            File file = new File(parent, script.getValue());
            if (file.isFile()) {
                scriptFile = new ScriptFile(file);
            } else {
                file = new File(script.getValue());
                if (file.isFile()) {
                    scriptFile = new ScriptFile(file, suffixForGenerationScript);
                    if (isScriptToCopy) {
                        this.externalFileToCopy.add(scriptFile);
                    }
                } else {
                    throw new IllegalStateException("File does not exists '" + script + "'");
                }
            }
        } else if (script instanceof ScriptFile2Generate) {
            scriptFile = new ScriptFile(new File(script.getValue()), suffixForGenerationScript);
            if (isScriptToCopy) {
                this.externalFileToCopy.add(scriptFile);
            }
        } else {
            throw new IllegalStateException("Unhandled type  '" + script.getClass() + "': " + script);
        }

        return scriptFile;
    }

    private void appendBracedKeyValue(final String key, final List<String> lines) {
        if (lines != null) {
            openBrace(key);
            for (String line : lines) {
                appendTab().append(line.toString()).append("\n");
            }
            closeBrace();
        }
    }

    private void appendBracedKeyValue(final String key, final String value) {
        if (value != null) {
            openBrace(key);
            appendTab().append(value.toString());
            if (!value.endsWith("\n")) {
                builder.append("\n");
            }
            closeBrace();
        }
    }

    private void appendQuotedKeyValue(final String key, final Object value) {
        if (value != null) {
            appendTab();
            builder.append(key).append(" ").append("\"").append(value.toString()).append("\"").append("\n");
        }
    }

    private void appendSemiColonKeyValue(final String key, final Object value) {
        if (value != null) {
            appendTab();
            builder.append("\"").append(key).append("\"").append(" : ").append("\"").append(value.toString())
                    .append("\"").append(",\n");
        }
    }

    private void appendUnquotedKeyValue(final String key, final Object value) {
        if (value != null) {
            appendTab();
            builder.append(key).append(" ").append(value.toString()).append("\n");
        }
    }

}
