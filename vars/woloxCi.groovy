import com.wolox.ProjectConfiguration
@Library('wolox-ci')
import com.wolox.parser.ConfigParser

def call(String yamlName) {
    def yaml = readYaml file: yamlName;

    def buildNumber = Integer.parseInt(env.BUILD_ID)

    // load project's configuration
    ProjectConfiguration projectConfig = ConfigParser.parse(yaml, env);

    def image = projectConfig.image.toLowerCase();

    // build the image specified in the configuration
    def customImage = docker.image(image);

    // adds the last step of the build.
    def closure = buildSteps(projectConfig, customImage);

    // each service is a closure that when called it executes its logic and then calls a closure, the next step.
    projectConfig.services.each {

        closure = "${it.service.getVar()}"(projectConfig, it.version, closure);
    }

    // we execute the top level closure so that the cascade starts.
    try {
        closure([:]);
    } finally {
        stage("cleanup") {
            deleteDockerImages(projectConfig);
        }
    }
}
