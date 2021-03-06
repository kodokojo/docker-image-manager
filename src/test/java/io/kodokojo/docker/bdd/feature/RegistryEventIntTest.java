package io.kodokojo.docker.bdd.feature;

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

import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.junit.ScenarioTest;
import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import io.kodokojo.docker.bdd.Registry;
import io.kodokojo.commons.bdd.stage.docker.DockerCommonsWhen;
import io.kodokojo.commons.bdd.stage.docker.DockerRegistryThen;
import io.kodokojo.docker.bdd.stage.ApplicationGiven;
import io.kodokojo.docker.bdd.stage.dockerfilebuildplan.DockerBuildPlanOrchestratorThen;
import io.kodokojo.docker.bdd.stage.restentrypoint.RestEntryPointThen;
import org.junit.Rule;
import org.junit.Test;

@As("RegistryEvent integration tests")
public class RegistryEventIntTest extends ScenarioTest<ApplicationGiven<?>, DockerCommonsWhen, RestEntryPointThen<?>> {

    @Rule
    public DockerPresentMethodRule dockerPresentMethodRule = new DockerPresentMethodRule();

    @Test
    @DockerIsRequire
    @Registry
    public void push_busybox_to_registry_and_get_valid_build_plan() {

        DockerRegistryThen<?> dockerRegistryThen = addStage(DockerRegistryThen.class);
        DockerBuildPlanOrchestratorThen<?> dockerBuildPlanOrchestratorThen = addStage(DockerBuildPlanOrchestratorThen.class);

        String parent = "centos:7";
        String image = "busybox:latest";

        given().$_is_pull(image)
                .and().$_is_pull("java:8-jre")
                .and().$_is_pull("registry:2")
                .and().kodokojo_docker_image_manager_is_started()
                .and().registry_send_notification_to_docker_image_manager()
                .and().registry_is_started();

        when().push_image_$_to_registry(image);

        then().repository_contain_a_Dockerfile_node_of_$_image_build_with_success(image);
        dockerRegistryThen
        .and().attach_docker_image_manager_logs();
        dockerBuildPlanOrchestratorThen
        .and().docker_build_plan_orchestrator_NOT_contain_a_DockerBuildPlan_for_image_$(parent);
    }

}
