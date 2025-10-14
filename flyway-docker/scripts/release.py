from constants import VARIANTS, REDGATE_OVERLAY
from build_images import _flyway_build_commands, _overlay_build_commands, _final_layer_commands
from docker_utils import Tag
import subprocess
import sys
import argparse

def get_suffixes(edition: str, version: str) -> tuple[list[str], list[Tag]]:
    use_buildx = (edition != REDGATE_OVERLAY.name)

    flyway_cmds, flyway_tags = _flyway_build_commands(edition, version, VARIANTS, use_buildx)

    overlay_cmds, overlay_tags = _overlay_build_commands(edition, flyway_tags)

    final_layer_cmds, final_layer_tags = _final_layer_commands(overlay_tags, use_buildx)

    cmds = overlay_cmds if edition == REDGATE_OVERLAY else flyway_cmds
    return cmds+final_layer_cmds, overlay_tags + final_layer_tags

def get_push_command(tag: str) -> str:
    return f'docker push {tag}'

def extend_tags(tag) -> tuple[str, list[str]]:
  return tag.as_docker_tag(), [tag.as_latest(), tag.as_major(), tag.as_minor()]

def get_buildx_push_command(buildx_command: str, tag: Tag) -> str:
    return f"""
  {buildx_command[:-4]}
  --output type=registry,name={tag.as_docker_tag()} \\
  --output type=registry,name={tag.as_latest()} \\
  --output type=registry,name={tag.as_major()} \\
  --output type=registry,name={tag.as_minor()} \\
  ."""

def get_tag_commands(source_tag: str, target_tag: str) -> str:
    return f"docker tag {source_tag} {target_tag}"

def _parse_args(argv: list[str]) -> argparse.Namespace:
    def bool_arg(value: str) -> bool:
      val = value.lower()
      if val in {"true", "1", "yes", "y"}:
        return True
      if val in {"false", "0", "no", "n"}:
        return False
      raise argparse.ArgumentTypeError(f"Invalid boolean value: {value}")
    parser = argparse.ArgumentParser(description="Release Flyway / Redgate Docker images.")
    parser.add_argument("edition", choices=["flyway", "redgate"], help="Which edition to release images for.")
    parser.add_argument("version", help="Flyway version (e.g. 10.20.0)")
    parser.add_argument("test_mode", type=bool_arg, help="If false, release. Otherwise publish to internal registry.")
    parser.add_argument("--dry-run", "-n", action="store_true", dest="dry_run", help="Print the planned docker commands without executing them.")
    return parser.parse_args(argv)

def main(argv: list[str]):
    args = _parse_args(argv)

    cmds, tags = get_suffixes(args.edition, args.version)

    registry = "docker.io/" if not args.test_mode else "redgate.azurecr.io/"
    edition = args.edition if not args.test_mode else f"{args.edition}-test"

    destination_tags = [x.with_registry(registry).with_edition(edition) for x in tags]
    release_commands = []
    if args.edition == REDGATE_OVERLAY.name:
        tag_commands = [get_tag_commands(x[0].as_docker_tag(),x[1].as_docker_tag()) for x in zip(tags, destination_tags)]
        release_commands += tag_commands
        for tag in destination_tags:
            base_tag, extended_tags = extend_tags(tag)
            release_commands.extend([get_tag_commands(base_tag, extended_tag) for extended_tag in extended_tags])
            release_commands.extend([get_push_command(extended_tag) for extended_tag in [base_tag, *extended_tags]])
    else:
        release_commands.extend([get_buildx_push_command(command, tag) for command, tag in zip(cmds, destination_tags)])

    if args.dry_run:
        print("Dry run: the following commands would be executed (in order):")

    for command in release_commands:
        print(command)
        if not args.dry_run:
            subprocess.run(command, check=True, shell=True)

if __name__ == "__main__":
    main(sys.argv[1:])
