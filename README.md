# Wolox CI

---
> **_Note:_** This repository differ from the upstream project in that, the build here starts based on a docker imgae (`image` property) instead of dockerfile (`dockerfile` property).
---

This a Jenkins library built to make it easier for us at Wolox to configure pipelines without necessarily knowing about Jenkinsfile syntax.
All our projects are built using a docker image.

## How to use:

**1. Configure your project on Jenkins server with the pipeline script:**

```
library identifier: 'wolox-ci@use-image-no-dockerfile',
        retriever: modernSCM([$class: 'GitSCMSource', remote: 'https://github.com/mhewedy/wolox-ci.git'])

node {
  checkout([
        $class: 'GitSCM', 
        branches: [[name: '**']],
        doGenerateSubmoduleConfigurations: false, 
        extensions: [], 
        submoduleCfg: [], 
        userRemoteConfigs: [[url: '<path to the git repository url>']]
    ])
  woloxCi('.pipeline.yaml');
}
```

example: 

<img src="https://github.com/mhewedy/wolox-ci/raw/use-image-no-dockerfile/etc/pipeline-jenkins-config.png"  width="700px"/>


It basically loads the library, clones the target repository and calls `woloxCi` to make its magic.
As an argument, `woloxCi` receives the path to a configuration yaml file (`.pipeline.yaml`).


**2. Add the `.pipeline.yaml` at the root of your project, sample file looks like this:**

```
config:
  project_name: hello-world
  image: golang

steps:
  build:
    - go build -o hello-world
    - chmod +x hello-world
  test:
    - ./hello-world
```

> No `Jenkinsfile` is needed to be part of the soruce code. the only file needed is `.pipeline.yaml`.

This is how it looks to run the above pipeline in the blueocean ui:

<img src="https://github.com/mhewedy/wolox-ci/raw/use-image-no-dockerfile/etc/test-result.png"  width="400px"/>

> Notice the `cleanup` stage has added automatically at the end of the build to cleanup resources.

See example [golang](https://github.com/mhewedy-playground/wolox-ci-examples/tree/golang) and [nodejs](https://github.com/mhewedy-playground/wolox-ci-examples/tree/nodejs) apps.

### multi-branch with Jenkins DSL plugin
Hence the pipeline is created using pipeline script (that reference the `.pipeline.yaml` file) then we cannot use a *Multibranch Pipeline* hence it requires a scm url to scan.

So, I think a Job DSL whould come to the resuce. see https://github.com/jenkinsci/job-dsl-plugin/pull/671

Other option is to use the "Multibranch pipeline" and to keep both the `Jenkinsfile` and `.pipeline.yaml` file in the repository.

## Documations for `.pipeline.yaml` sections:

### Configuration

The section under the `config` label defines some basic configuration for this project:
1. The docker image will used to run the build, the default value is `alpine`
2. The project name.

### Services

This section lists the project's dependencies. Each section will define and expose some environment variables that might be needed by the application:

#### Microsoft SQL

When listing `mssql` as a service, this will build a docker image from [`microsoft/mssql-server-linux`](https://hub.docker.com/r/microsoft/mssql-server-linux/) exposing the following environment variables:

1. DB_USERNAME
2. DB_PASSWORD
3. DB_HOST
4. DB_PORT

#### PostgreSQL

When listing `postgres` as a service, this will build a docker image from [`postgres`](https://hub.docker.com/_/postgres/) exposing the following environment variables:

1. DB_USERNAME
2. DB_PASSWORD
3. DB_HOST
4. DB_PORT

#### Redis

When listing `redis` as a service, this will build a docker image from [`redis`](https://hub.docker.com/_/redis/) exposing the following environment variables:

1. REDIS_URL: the redis url that looks like this `redis://redis`

#### MySQL

When listing `mysql` as a service, this will build a docker image from [`mysql`](https://hub.docker.com/_/mysql/) exposing the following environment variables:

1. DB_USERNAME
2. DB_PASSWORD
3. DB_HOST
4. DB_PORT

#### MongoDB

When listing `mongo` as a service, this will build a docker image from [`mongo`](https://hub.docker.com/_/mongo/) exposing the following environment variables:

1. DB_USERNAME
2. DB_PASSWORD
3. DB_HOST
4. DB_PORT

#### Elasticsearch

When listing `elasticsearch:6.4.0` as a service (it's mandatory specify the image version), this will build a docker image from [`elasticsearch`](https://www.docker.elastic.co/) exposing the following environment variables:

1. ELASTICSEARCH_URL

### Steps

This section lets you define the steps you need to build your project. Each level inside the `steps` section is a stage in the Jenkins pipeline and each item in the stage is a command to run. In the case above, there are 5 stages named:

1. analysis
2. setup_db
3. test
4. security
5. audit


The analysis stage, for example, runs the following commands:

1. `bundle exec rubocop -R app spec --format simple`
2. `bundle exec rubycritic --path ./analysis --minimum-score 80 --no-browser`


### Environment

This section lets you set up custom environment variables. Each item inside this section defines a variable with its value.

### Timeout

This section lets you set up a custom timeout in seconds. The default is 600 (10 minutes).
