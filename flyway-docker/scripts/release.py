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
        # Multi-arch images are pushed using the buildx command with --push for each base variant.
        variants = [
            ("", "base"),
            ("-alpine", "alpine"),
            ("-azure", "azure"),
        ]
        for suffix, folder in variants:
            release_commands.append(get_buildx_command(edition, version, suffix, folder) + " --push")

        # Push tags (latest, major.minor, major) for all variant combinations (base, alpine, azure) and mongo layers.
        variant_suffixes = [
            "",  # base
            "-alpine",
            "-azure",
            "-mongo",
            "-alpine-mongo",
            "-azure-mongo",
        ]
        for suf in variant_suffixes:
            tags.extend(utils.generate_tags(version, suf))
    else:
        # For redgate, push all tags for all variants
        tags.extend(utils.generate_tags(version, ""))
        tags.extend(utils.generate_tags(version, "-mongo"))
        tags.extend(utils.generate_tags(version, "-alpine"))
        tags.extend(utils.generate_tags(version, "-alpine-mongo"))
        tags.extend(utils.generate_tags(version, "-azure"))
        tags.extend(utils.generate_tags(version, "-azure-mongo"))

    release_commands.extend([get_push_command(edition, tag) for tag in tags])

    for command in release_commands:
        print(command)
        subprocess.run(command, check=True, shell=True)
