import json
import os
import sys
import shlex
from subprocess import PIPE, Popen

group = sys.argv[1]


def get_deployments():
    get_deployments_cmd = "kubectl get deployments --namespace=egov -l " \
                          "group={}".format(group)
    filter_headers_cmd = "grep -v NAME"
    awk_cmd = "awk {'print $1'}"
    deployments_verbose = Popen(shlex.split(get_deployments_cmd), stdout=PIPE)
    deployments_verbose_without_headers = Popen(shlex.split(
        filter_headers_cmd), stdin=deployments_verbose.stdout, stdout=PIPE)
    deployment_names = Popen(shlex.split(awk_cmd),
                             stdin=deployments_verbose_without_headers.stdout,
                             stdout=PIPE)
    deployments, error = deployment_names.communicate()
    if error:
        raise Exception("Deployments doesn't exist for group {}\nERROR:{}"
                        .format(group, error))
    return deployments.strip().split("\n")


def get_image_tags(deployments):
    get_kube_config_cmd = "cat /.kube/config"
    out,err = Popen(shlex.split(get_kube_config_cmd)).communicate()
    print "$$$$$$"
    print out, err
    image_tags = {}
    for d in deployments:
        deployment_cmd = "kubectl get deployment {} -o json " \
                         "--namespace=egov".format(d)
        deployment_config = Popen(shlex.split(deployment_cmd), stdout=PIPE)
        deployment_json, error = deployment_config.communicate()
        print "fubar"
        print deployment_json, error
        if error:
            raise Exception("Cannot find deployment config for {}\nERROR:{}".
                            format(d, error))
        deployment = json.loads(deployment_json)
        containers = deployment['spec']['template']['spec']['containers']
        for c in containers:
            name = str(c['name'])
            image = str(c['image'])
            image_tags[name] = image
    return image_tags


def set_kubectl_env():
    get_kube_config_cmd = "cat /.kube/config"
    out,err = Popen(shlex.split(get_kube_config_cmd)).communicate()
    print "!!!!!"
    print out, err
    kube_server_url = os.getenv("KUBE_SERVER_URL")
    if not kube_server_url:
        raise Exception("KUBE_SERVER_URL env var is not set")
    set_kubectl_env_cmd = ("kubectl config set-cluster env --server {}"
                           .format(kube_server_url))
    out, err = Popen(shlex.split(set_kubectl_env_cmd)).communicate()
    if err:
        raise Exception("kube server url could not be set\nERROR:{}"
                        .format(err))


def main():
    set_kubectl_env()
    deployments = get_deployments()
    tags = get_image_tags(deployments)
    os.remove("image_tags.txt")
    with open("image_tags.txt", "w") as f:
        f.write(json.dumps(tags))


if __name__ == "__main__":
    main()
