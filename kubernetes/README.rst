.. _Kubernetes Library:
---------
Kubernetes
---------

This library allows you to perform deployments to static or ephemeral Kubernetes application environments with
Helm_


Steps Provided
##############

* deploy_to
* ephemeral

Library Configurations
######################

The configurations for the Kubernetes library can be specified in the library spec or on a per application environment. 

Kubernetes Credential and Context
*********************************

The Kubernetetes Credential is the Jenkins credential defined as a Secrets file that holds the kubeconfig fie contents with access information to the kubernetes target environments.
The Kubernetes Context is the context within the kubeconfig that should be used to identify the target environment  for deployment

You would specify this as follows:

.. code:: groovy

    application_environments{
      dev{
        short_name = "dev"
        long_name = "Development"
      }
      test{
        short_name = "test"
        long_name = "Test"
        k8s_context = "test"
      }
      prod{
        short_name = "prod"
        long_name = "Production"
        k8s_credential = "cluster1-config"
        k8s_context = "production"
      }
    }
    libraries{
      kubernetes{
        k8s_credential = "cluster2-config"
        k8s_context = "dev"
      }
    }

With this configuration, ``dev`` context within the  ``cluster2-config`` would be used when deploying
to ``dev`` and the ``test`` context within the  ``cluster2-config`` would be used when deploying
to  ``test`` while ``production`` context within the ``cluster1-config`` would be used when
deploying to ``prod``. Together k8s_credential and k8s_context uniquely identify the target environment for deployment.

Helm Configuration
******************

We use Helm_ for a deployment mechanism to Kubernetes.  Helm is a package manager and templating
engine for Kubernetes manifests.  Using Helm, the typical YAML manifests used to deploy to
Kubernetes distributions can be templatized for reuse.  In our case, a different values file is
used for each static application environment.

**Deploy the Tiller Server**

You can configure Helm for multitenancy by using our
:ref:`environment provisioning script <helm environment provisioning script>`.

Instead of using Helm as a package manager by bundling the charts and deploying them to a chart
repository, we instead use a configuration repository as our infrastructure as code mechanism.

**Create Helm Configuration Repository**

You'll need to create a GitHub repository to store the helm chart for your application(s). See the
`helm docs on provisioning a new chart`_ to get intialize the repository with the skeleton for your chart.

How you choose to build your helm chart is up to you, you can put every api object in the ``templates``
directory or have subcharts for each individual microservice.  SDP doesn't care, as all it does is clone
the github repository and deploy the chart using the specified values file.

**Values File Conventions**

Given that we tag container images using the git SHA, SDP will clone your helm configuration repository and
update a key corresponding to the current version of each container image for each application environment.

As such, a certain syntax is required in your values file.  You must have an ``image_shas`` key. SDP will
automatically add subkeys for each repositories under this ``image_shas`` with a value that is the git SHA.

`Given that YAML keys can't have hypens, hyphens in repository names will be replaced with underscores.`

.. code:: yaml

    image_shas:
      my_sample_application: abcdefgh
      another_repo: abcdef

you can add whatever other keys are necessary to appropriately parameterize your helm chart.

**Helm Configurations for the Library**

The helm configuration repository, github credential, tiller credential namespace  can be configured
globally in the library spec and overriden for specific application environments.

The values file to will default to ``values.${app_env.short_name}.yaml``, or can be overridden via ``app_env.chart_values_file``.

The name of the release will default to ``app_env.short_name``, or can be overridden via ``app_env.tiller_release_name``

An example of helm configurations:

.. code:: groovy

    application_environments{
      dev{
        short_name = "dev"
        long_name = "Development"
        chart_values_file = "dev_values.yaml"
      }
      test{
        short_name = "test"
        long_name = "Test"
        tiller_release_name = "banana"

      }
      prod{
        short_name = "prod"
        long_name = "Production"
        tiller_namespace = "rhs-tiller-prod"
      }
    }
    libraries{
      kubernetes{
        helm_configuration_repository = "https://github.boozallencsn.com/Red-Hat-Summit/helm-configuration.git"
        helm_configuration_repository_credential = "github"
        tiller_namespace = "rhs-tiller"
      }
    }

Promoting Images
****************

It's often beneficial to build a container image once, and then promote that image
through different application environments. This makes it possible to test the
content of an image once in a lower environment, and remain confident that the
results of those tests would be the same as an image is promoted. Promoting
images also speeds up the CI/CD pipeline, as building a container image is often
the most time-consuming part of the pipeline.

By default, the ``deploy_to()`` step of the kubernetes pipeline library will
promote a container image if it can expect one to exist, which is when the most
recent code change was a **merge** into the given code branch. The image would
be expected to be built from an earlier commit, or while there was an open PR.

You can override this default for the entire pipeline by setting the
``promote_previous_image`` config setting to **false**. You can also choose
whether or not to promote images for each application environment individually
through the ``promote_previous_image`` application_environment setting. This
app_env setting takes priority over the config setting.

An example of these settings' usage:

.. code:: groovy

    application_environments{
      dev{
        short_name = "dev"
        long_name = "Development"
        promote_previous_image = false
      }
      prod{
        short_name = "prod"
        long_name = "Production"
      }
    }
    libraries{
      kubernetes{
        helm_configuration_repository = "https://github.boozallencsn.com/Red-Hat-Summit/helm-configuration.git"
        helm_configuration_repository_credential = "github"
        tiller_namespace = "rhs-tiller"
        k8s_credential = "cluster1-config"
        k8s_context = "staging"
        promote_previous_image = true //note: making this setting true is redundant, since true is the default
      }
    }

Putting It All Together
***********************


.. csv-table:: Kubernetes Library Configuration Options
   :header: "Field", "Description", "Default Value", "Defined On"

   "k8s_credential", "The Jenkins credential ID defined as a Secrets File that holds the kubeconfig file", , "both"
   "helm_configuration_repository", "The GitHub Repository containing the helm chart(s) for this application", ,"both"
   "helm_configuration_repository_credential", "The Jenkins credential ID to access the helm configuration GitHub repository", , "both"
   "tiller_namespace", "The tiller namespace for this application", , "both"
   "k8s_context", "The Jenkins credential ID specifying the context within the k8s_credential kubeconfig that identifies the target environment", , "both"
   "tiller_release_name", "The name of the release to deploy", , "app env"
   "chart_values_file", "The values file to use for the release", , "app_env"
   "promote_previous_image", "Whether or not to promote a previously-built image", "(Boolean) true", "both"


.. code:: groovy

    application_environments{
      dev{
        short_name = "dev"
        long_name = "Development"
        chart_values_file = "dev_values.yaml"
      }
      test{
        short_name = "test"
        long_name = "Test"
        tiller_release_name = "banana"
        k8s_credential = "test-context"

      }
      prod{
        short_name = "prod"
        long_name = "Production"
        tiller_namespace = "rhs-tiller-prod"
        k8s_credential = "prod-clusters"
        k8s_context = "canary-context"
        promote_previous_image = true
      }
    }
    libraries{
      kubernetes{
        k8s_credential = "dev-test-clusters"
        helm_configuration_repository = "https://github.boozallencsn.com/Red-Hat-Summit/helm-configuration.git"
        helm_configuration_repository_credential = "github"
        tiller_namespace = "rhs-tiller"
        k8s_credential = "dev-context"
        promote_previous_image = false
      }
    }

External Dependencies
#####################

* Target Kubernetes cluster is deployed and accessible from Jenkins
* Helm configuration repository creates
* Values files contain the ``image_shas`` key convention
* A Jenkins credential exists to access helm configuration repository
* A Jenkins credential exists holding the kubeconfig file
* A Jenkins credential exists specifying the current context within the kubeconfig


.. _Helm: https://helm.sh/
.. _helm docs on provisioning a new chart: https://docs.helm.sh/helm/#helm-create
