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
    p.add_argument("--env-pass", metavar="VAR", action="append", help="Pass-through environment variable to container (e.g. AWS_REGION)")
    p.add_argument("--dry-run", "-n", action="store_true", help="Print planned docker run commands without executing")
    return p.parse_args(argv)

def _env_pass(args: List[str] | None) -> str:
    if not args:
        return ""
    return " ".join([f"-e {arg}={os.getenv(arg, "None")}" for arg in args])

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

    if 'MONGO_CONNECTION_DETAILS' not in os.environ:
        missing_errors.append('MONGO_CONNECTION_DETAILS (required for mongo tests)')
    if missing_errors and not args.dry_run:
        raise SystemExit('Missing required env vars: ' + ', '.join(missing_errors))

    if not args.dry_run:
        for img in images:
            ensure_image(img)

    flyway_commands = ["info", "migrate", "clean"]
    if edition == "redgate":
        flyway_commands += [
            "check -code",
            "check -changes",
        ]

    test_sql_path = os.getcwd() + "/test-sql"
    conf_path = os.getcwd() + f"/conf/{edition}"

    planned_commands: List[str] = []
    for image in images:
        env_var_flag = _env_pass(args.env_pass)
        environment = "default"
        if "mongo" in image:
          environment = "mongo"
        if "oracle" in image:
          environment = "oracle"
          env_var_flag += " -e FLYWAY_NATIVE_CONNECTORS=true"
        flyway = "flyway" if "-azure" in image else ""
        for flyway_command in flyway_commands:
            planned_commands.append(
                f'docker run --rm --network=host -v "{test_sql_path}:/flyway/sql" -v {conf_path}:/flyway/conf {env_var_flag} {image} {flyway} -environment={environment} {flyway_command}'.strip()
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
