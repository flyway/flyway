import utils
from build_images import get_buildx_command
import subprocess
import sys


def get_push_command(edition, tag):
    return f'docker push {edition}/flyway:{tag}'


if __name__ == "__main__":
    edition = sys.argv[1]
    version = sys.argv[2]

    release_commands = []
    tags = []
    if edition == "flyway":
        # Multi-arch images are pushed using the buildx command
        release_commands.append(get_buildx_command(edition, version, "", ".", False, True))
        tags.extend(utils.generate_tags(version, "-alpine"))
        tags.extend(utils.generate_tags(version, "-azure"))
        release_commands.append(get_buildx_command(edition, version, "", ".", True, True))
        tags.extend(utils.generate_tags(version, "-alpine-mongo"))
        tags.extend(utils.generate_tags(version, "-azure-mongo"))
    else:
        tags.extend(utils.generate_tags(version, ""))
        tags.extend(utils.generate_tags(version, "-alpine"))
        tags.extend(utils.generate_tags(version, "-azure"))

    release_commands.extend([get_push_command(edition, tag) for tag in tags])

    for command in release_commands:
        print(command)
        subprocess.run(command, check=True, shell=True)
