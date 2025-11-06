import sys
import os
import subprocess
from typing import List
import argparse
from docker_utils import ensure_image, get_all_tags
from cli_utils import  bool_arg

def _parse_args(argv: List[str]) -> argparse.Namespace:
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

    tags = get_all_tags(test_mode, edition, version)

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
        flyway = "flyway" if "-azure" in image else ""
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
