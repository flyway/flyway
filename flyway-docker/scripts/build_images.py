import sys
import os
import subprocess
import tempfile
from typing import List
import argparse
import docker_utils
from docker_utils import Tag
from constants import VARIANTS, REDGATE_OVERLAY, FINAL_LAYERS


def _pre_build_commands(use_buildx: bool, clean_buildx: bool) -> list[str]:
    commands = []
    if use_buildx:
        if clean_buildx:
            commands.append("docker buildx ls | grep -q 'multi_arch_builder' && docker buildx rm multi_arch_builder")
        commands.extend([
            "docker run --rm --privileged multiarch/qemu-user-static --reset -p yes",
            "docker buildx create --name multi_arch_builder --driver docker-container --driver-opt network=bridge --use",
        ])
    return commands


def _flyway_build_commands(edition: str, version: str, variants: list, use_buildx: bool) -> tuple[list[str], list[Tag]]:
    fn = docker_utils.get_buildx_command if use_buildx else docker_utils.get_build_command
    result = [fn(edition, version, variant) for variant in variants] if variants else []
    commands, tags = zip(*result)
    return list(commands), list(tags)


def _overlay_build_commands(edition: str, flyway_tags: list[Tag]) -> tuple[list[str], list[Tag]]:
    commands = []
    overlay_tags = []
    if edition == REDGATE_OVERLAY.name:
        sqlfluff_version = os.environ.get('SQLFLUFF_VERSION')
        if not sqlfluff_version:
            raise SystemExit("Error: Environment variable SQLFLUFF_VERSION must be set when building redgate edition images.")
        for tag in flyway_tags:
            # Create overlay tag with redgate edition, same version and variant
            overlay_tag = docker_utils.Tag(edition=REDGATE_OVERLAY.name, version=tag.version, variant=tag.variant)
            overlay_tags.append(overlay_tag)
            redgate_dockerfile = REDGATE_OVERLAY.dockerfiles[tag.variant.name]
            extra_args = {"SQLFLUFF_VERSION": sqlfluff_version}
            commands.append(docker_utils.get_custom_build_command(
                image_tag=overlay_tag,
                base_tag=tag,
                dockerfile=redgate_dockerfile,
                extra_build_args=extra_args,
            ))
        return commands, overlay_tags
    else:
        return [], flyway_tags


def _final_layer_commands(tags: list[Tag], use_buildx: bool) -> list[str]:
    commands = []
    layer_tags = []
    for tag in tags:
        for layer in FINAL_LAYERS:
            if layer.applies(tag.as_docker_tag(), tag.edition, tag.variant):
                layer_command, layer_tag = docker_utils.layer_command(tag, layer, use_buildx)
                commands.append(layer_command)
                layer_tags.append(layer_tag)
    return commands, layer_tags


def _compute_plan(edition: str, version: str, test_mode: bool, clean_buildx: bool = False) -> list[tuple[str, list[str]]]:
    """Generate ordered docker build commands based on declarative config."""
    # Determine build mode
    use_buildx = (edition != REDGATE_OVERLAY.name) and (not test_mode)
    variants = [v for v in VARIANTS if (not test_mode) or v.name == "base"]

    plan = []
    pre_build_cmds = _pre_build_commands(use_buildx, clean_buildx)
    if pre_build_cmds:
        plan.append(("Pre-Build Commands", pre_build_cmds))

    flyway_cmds, flyway_tags = _flyway_build_commands(edition, version, variants, use_buildx)
    if flyway_cmds:
        plan.append(("Flyway Build Commands", flyway_cmds))

    overlay_cmds, overlay_tags = _overlay_build_commands(edition, flyway_tags)
    if overlay_cmds:
        plan.append(("Overlay Build Commands", overlay_cmds))

    final_layer_cmds, _ = _final_layer_commands(overlay_tags, use_buildx)
    if final_layer_cmds:
        plan.append(("Final Layer Commands", final_layer_cmds))

    return plan


def _parse_args(argv: List[str]) -> argparse.Namespace:
    def bool_arg(value: str) -> bool:
        val = value.lower()
        if val in {"true", "1", "yes", "y"}:
            return True
        if val in {"false", "0", "no", "n"}:
            return False
        raise argparse.ArgumentTypeError(f"Invalid boolean value: {value}")

    parser = argparse.ArgumentParser(description="Build Flyway / Redgate Docker images.")
    parser.add_argument("edition", choices=["flyway", "redgate"], help="Which edition to build images for.")
    parser.add_argument("version", help="Flyway version (e.g. 10.20.0)")
    parser.add_argument("test_mode", type=bool_arg, help="If true, only build base variant single-arch for quick testing.")
    parser.add_argument("--dry-run", "-n", action="store_true", dest="dry_run", help="Print the planned docker commands without executing them.")
    parser.add_argument('--clean-buildx', action='store_true', default=False, help='Remove multi_arch_builder before building (default: False)')
    parser.add_argument('--verbose', action='store_true', help='Always print command output, even if successful.')
    return parser.parse_args(argv)


def _validate_version(version: str) -> None:
    # Lightweight validation: digits and dots only (optionally a leading 'v')
    import re
    if not re.fullmatch(r"v?\d+(\.\d+)*(-SNAPSHOT)?", version):
        print(f"Warning: version '{version}' does not match typical pattern (e.g. 10.2.0 or 10.2.0-SNAPSHOT)", file=sys.stderr)


def main(argv: List[str]):
    args = _parse_args(argv)
    verbose = args.verbose  # Add verbose flag
    _validate_version(args.version)
    plan = _compute_plan(edition=args.edition, version=args.version, test_mode=args.test_mode, clean_buildx=args.clean_buildx)
    if args.dry_run:
        print("Dry run: the following commands would be executed (in order):")
        for stage_name, commands in plan:
            print(f"\nStage: {stage_name}")
            for i, cmd in enumerate(commands, 1):
                print(f"  {i:02d}. {cmd}")
        return
    for stage_name, commands in plan:
        print(f"\nExecuting stage: {stage_name}")
        for command in commands:
            print(f"Running docker build command: {command}")
            with tempfile.NamedTemporaryFile(delete=False, mode='w+', encoding='utf-8') as tmpfile:
                result = subprocess.run(command, shell=True, stdout=tmpfile, stderr=tmpfile)

            if verbose or result.returncode != 0:
                with open(tmpfile.name, 'r', encoding='utf-8') as tmpfile_read:
                    output = tmpfile_read.read()
                    print(output)

            if result.returncode != 0:
                os.unlink(tmpfile.name)
                raise subprocess.CalledProcessError(result.returncode, command)
            os.unlink(tmpfile.name)

if __name__ == "__main__":
    # Pass sys.argv[1:] to retain ability for external tooling to supply positional args
    main(sys.argv[1:])
