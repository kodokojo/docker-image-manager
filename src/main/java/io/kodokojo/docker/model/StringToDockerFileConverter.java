package io.kodokojo.docker.model;

/*
 * #%L
 * docker-image-manager
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.model.StringToImageNameConverter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isBlank;

public class StringToDockerFileConverter  {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringToDockerFileConverter.class);

    private static final Pattern FROM_PATTERN = Pattern.compile("^FROM (.*)$");

    private static final Pattern MAINTAINER_PATTERN = Pattern.compile("^MAINTAINER (.*)$");

    public static DockerFile convertToDockerFile(ImageName imageName, String content) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        if (isBlank(content)) {
            throw new IllegalArgumentException("content must be defined.");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to convert image {} with following content: \n{}", imageName.getFullyQualifiedName(), content);
        }
        String[] splitted = content.split("\n");
        List<String> lines = Arrays.asList(splitted);
        Iterator<String> it = lines.iterator();
        String from = null;
        String maintainer = null;
        while((from == null || maintainer == null) && it.hasNext()) {
            String line = it.next();
            Matcher fromMatcher = FROM_PATTERN.matcher(line);
            if (from == null && fromMatcher.find()) {
                from = fromMatcher.group(1);
            } else {
                Matcher maintainerMatcher = MAINTAINER_PATTERN.matcher(line);
                if (maintainerMatcher.find()) {
                    maintainer = maintainerMatcher.group(1);
                }
            }
        }
        ImageName fromImageName = StringUtils.isNotBlank(from) ? StringToImageNameConverter.convert(from) : null;
        DockerFile res = new DockerFile(imageName, fromImageName, maintainer);
        return res;
    }

}