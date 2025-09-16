from typing import List
from constants import VARIANTS, REDGATE_OVERLAY
from build_images import _flyway_build_commands, _overlay_build_commands, _final_layer_commands
from itertools import chain
import subprocess
import sys
import argparse

def get_suffixes(edition: str, version: str):
    use_buildx = (edition != REDGATE_OVERLAY.name)

    flyway_cmds, flyway_tags = _flyway_build_commands(edition, version, VARIANTS, use_buildx)

    overlay_cmds, overlay_tags = _overlay_build_commands(edition, flyway_tags)

    final_layer_cmds, final_layer_tags = _final_layer_commands(overlay_tags, use_buildx)

    cmds = overlay_cmds if edition == REDGATE_OVERLAY else flyway_cmds
    return cmds+final_layer_cmds, overlay_tags + final_layer_tags

def get_push_command(tag):
    return f'docker push {tag}'

def extend_tags(tag):
  return tag.as_docker_tag(), [tag.as_latest(), tag.as_major(), tag.as_minor()]

def get_buildx_push_command(buildx_command, tag):
    return f"""
  {buildx_command[:-4]}
  --output type=registry,name=docker.io/{tag.as_docker_tag()} \\
  --output type=registry,name=docker.io/{tag.as_latest()} \\
  --output type=registry,name=docker.io/{tag.as_major()} \\
  --output type=registry,name=docker.io/{tag.as_minor()} \\
  ."""

def get_tag_commands(source_tag, target_tag):
    return f"docker tag {source_tag} {target_tag}"

def _parse_args(argv: List[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Release Flyway / Redgate Docker images.")
    parser.add_argument("edition", choices=["flyway", "redgate"], help="Which edition to release images for.")
    parser.add_argument("version", help="Flyway version (e.g. 10.20.0)")
    parser.add_argument("--dry-run", "-n", action="store_true", dest="dry_run", help="Print the planned docker commands without executing them.")
    return parser.parse_args(argv)

def main(argv: List[str]):
    args = _parse_args(argv)

    cmds, tags = get_suffixes(args.edition, args.version)
    print(cmds)
    tags = [x.without_registry() for x in tags]
    print([x.as_docker_tag() for x in tags])

    release_commands = []
    if args.edition == REDGATE_OVERLAY.name:
        for tag in tags:
            base_tag, extended_tags = extend_tags(tag)
            release_commands.extend([get_tag_commands(base_tag, extended_tag) for extended_tag in extended_tags])
            release_commands.extend([get_push_command(extended_tag) for extended_tag in [base_tag, *extended_tags]])
    else:
        release_commands.extend([get_buildx_push_command(command, tag) for command, tag in zip(cmds, tags)])

    if args.dry_run:
        print("Dry run: the following commands would be executed (in order):")

    for command in release_commands:
        print(command)
        if not args.dry_run:
            subprocess.run(command, check=True, shell=True)

if __name__ == "__main__":
    main(sys.argv[1:])
