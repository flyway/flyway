import sys
import utils
import subprocess


def get_tag_flags(version, edition, tag_suffix, mongo=False):
    qualifier = ""
    if mongo:
        qualifier = "-mongo"
    tags = utils.generate_tags(version, tag_suffix)
    command_flags = ""
    for tag in tags:
        command_flags += f'-t {edition}/flyway:{tag}{qualifier} '
    return command_flags


def get_buildx_command(edition, version, tag_suffix, folder, mongo=False, push=False):
    pull_or_push = "pull"
    if push:
        pull_or_push = "push"
    platforms = "linux/arm64/v8,linux/amd64"
    target = edition if not mongo else "mongo"
    command = f'docker buildx build --target {target} --platform {platforms} --{pull_or_push} --build-arg FLYWAY_VERSION={version} --build-arg EDITION={edition} '
    command += get_tag_flags(version, edition, tag_suffix, mongo)
    file_flag = f'-f ./dockerfiles/{folder}/Dockerfile '
    return command + file_flag + "."
    
    
def get_build_command(edition, version, tag_suffix, folder, mongo=False):
    target = edition if not mongo else "mongo"
    command = f'docker build --target {target} --pull --build-arg FLYWAY_VERSION={version} --build-arg EDITION={edition} '
    command += get_tag_flags(version, edition, tag_suffix, mongo)
    file_flag = f'-f ./dockerfiles/{folder}/Dockerfile '
    return command + file_flag + "."
    

if __name__ == "__main__":
    edition = sys.argv[1]
    version = sys.argv[2]
    linux_only = sys.argv[3] == "True"
    commands = []

    if linux_only:
        commands.append(get_build_command(edition, version, "", "."))
        commands.append(get_build_command(edition, version, "", ".", True))
    else:
        if edition == "flyway":  # We only do multi-arch builds for OSS due to compatibility issues with Redgate Compare
            subprocess.run("docker buildx rm multi_arch_builder", shell=True)
            commands.append("docker run --rm --privileged multiarch/qemu-user-static --reset -p yes")
            commands.append("docker buildx create --name multi_arch_builder --driver docker-container --driver-opt network=bridge --use")
            commands.append(get_buildx_command(edition, version, "", "."))
            commands.append(get_buildx_command(edition, version, "", ".", True))
            commands.append(get_buildx_command(edition, version, "-alpine", "alpine"))
            commands.append(get_buildx_command(edition, version, "-alpine", "alpine", True))
        else:
            commands.append(get_build_command(edition, version, "", "."))
            commands.append(get_build_command(edition, version, "", ".", True))
            commands.append(get_build_command(edition, version, "-alpine", "alpine"))
            commands.append(get_build_command(edition, version, "-alpine", "alpine", True))

        commands.append(get_build_command(edition, version, "-azure", "azure"))
        commands.append(get_build_command(edition, version, "-azure", "azure", True))

    for command in commands:
        print(f'Running docker build command: {command}')
        subprocess.run(command, check=True, shell=True)
