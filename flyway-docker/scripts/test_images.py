import sys
import os
import subprocess
from typing import List
import argparse
from docker_utils import Tag, get_layer_image_tag
from constants import VARIANTS, REDGATE_OVERLAY, FINAL_LAYERS


def ensure_image(tag: str) -> None:
    """Fail fast if an expected image tag is not present locally."""
    inspect_cmd = f'docker image inspect {tag} >$null 2>&1' if os.name == 'nt' else f'docker image inspect {tag} >/dev/null 2>&1'
    result = subprocess.run(inspect_cmd, shell=True)
    if result.returncode != 0:
        raise SystemExit(f"Required image '{tag}' not found locally. Run build_images.py first for edition/version (got edition tag missing).")


def _parse_args(argv: List[str]) -> argparse.Namespace:
    def bool_arg(value: str) -> bool:
        v = value.lower()
        if v in {"true", "1", "yes", "y"}:
            return True
        if v in {"false", "0", "no", "n"}:
            return False
        raise argparse.ArgumentTypeError(f"Invalid bool: {value}")

    p = argparse.ArgumentParser(description="Run functional tests against built Flyway / Redgate images.")
    p.add_argument("edition", choices=["flyway", "redgate"], help="Edition to test")
    p.add_argument("version", help="Version tag used when building images")
    p.add_argument("test_mode", type=bool_arg, help="If true, test only base variant (plus mongo)")
    p.add_argument("--env-pass", metavar="VAR", help="Pass-through environment variable to container (e.g. AWS_REGION)")
    p.add_argument("--dry-run", "-n", action="store_true", help="Print planned docker run commands without executing")
    return p.parse_args(argv)


def main(argv: List[str]):
    args = _parse_args(argv)

    edition = args.edition
    version = args.version
    test_mode = args.test_mode

    use_buildx = (edition != REDGATE_OVERLAY.name) and (not test_mode)
    variants = [v for v in VARIANTS if (not test_mode) or v.name == "base"]

    if use_buildx:
      tags = [Tag(edition=edition, version=version, variant=v, registry="redgate.azurecr.io/") for v in variants]
    else:
      tags = [Tag(edition=edition, version=version, variant=v) for v in variants]

    # Add mongo variant (using base variant with mongo qualifier)
    base_tag = next(t for t in tags if t.variant.name == "base")
    final_layer_tags = [get_layer_image_tag(base_tag, layer) for layer in FINAL_LAYERS]
    tags.append(*final_layer_tags)

    # Convert to image strings for existing functions that expect strings
    images: List[str] = [tag.as_docker_tag() for tag in tags]

    # Env validation (skip hard failure on dry-run to allow planning)
    missing_errors: List[str] = []
    if edition == 'redgate' and 'FLYWAY_LICENSE_KEY' not in os.environ:
        missing_errors.append('FLYWAY_LICENSE_KEY (required for redgate)')
    if 'MONGO_CONNECTION_DETAILS' not in os.environ:
        missing_errors.append('MONGO_CONNECTION_DETAILS (required for mongo tests)')
    if missing_errors and not args.dry_run:
        raise SystemExit('Missing required env vars: ' + ', '.join(missing_errors))

    if not args.dry_run:
        for img in images:
            ensure_image(img)

    flyway_commands = ["info", "migrate", "clean -cleanDisabled=false"]
    flyway_cli_params = "-url=jdbc:sqlite:test "
    if edition == "redgate":
        flyway_cli_params += f'-licenseKey={os.environ.get("FLYWAY_LICENSE_KEY", "<unset>")} '
        flyway_commands += [
            "check -code -reportFilename=report",
            "check -changes -check.buildUrl=jdbc:sqlite:temp -reportFilename=report -cleanDisabled=false",
        ]

    test_sql_path = os.getcwd() + "/test-sql"
    env_var_flag = f'-e {args.env_pass}={os.getenv(args.env_pass)}' if args.env_pass else ""

    planned_commands: List[str] = []
    for i, image in enumerate(images):
        flyway = "flyway" if "azure" in image else ""
        for flyway_command in flyway_commands:
            planned_commands.append(
                f'docker run --rm -v "{test_sql_path}:/flyway/sql" {env_var_flag} {image} {flyway} {flyway_command} {flyway_cli_params}'.strip()
            )
        if i == len(images) - 1:
            flyway_cli_params_mongo = f'-url={os.getenv("MONGO_CONNECTION_DETAILS", "<unset>")} -sqlMigrationSuffixes=".js" -cleanDisabled=false '
            for flyway_command in ["clean", "info", "migrate"]:
                planned_commands.append(
                    f'docker run --rm -v "{test_sql_path}:/flyway/sql" {env_var_flag} {image} {flyway} {flyway_command} {flyway_cli_params_mongo}'.strip()
                )

    if args.dry_run:
        print("Dry run: the following test commands would execute (order preserved):")
        for i, c in enumerate(planned_commands, 1):
            print(f"{i:02d}. {c}")
        if missing_errors:
            print("\nNOTE: Missing env vars ->", ', '.join(missing_errors))
        return

    for cmd in planned_commands:
        print(cmd)
        subprocess.run(cmd, check=True, shell=True)


if __name__ == "__main__":
    main(sys.argv[1:])
