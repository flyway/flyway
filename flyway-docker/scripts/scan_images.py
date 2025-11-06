import sys
import os
import subprocess
from typing import List
import argparse
from docker_utils import ensure_image, get_all_tags
from cli_utils import bool_arg

def _parse_args(argv: List[str]) -> argparse.Namespace:
  p = argparse.ArgumentParser(description="Run Snyk scan against built Flyway / Redgate images.")
  p.add_argument("edition", choices=["flyway", "redgate"], help="Edition to test")
  p.add_argument("version", help="Version tag used when building images")
  p.add_argument("test_mode", type=bool_arg, help="If true, test only base variant (plus mongo)")
  p.add_argument("snyk_path", help="Path to the .snyk folder")
  p.add_argument("--dry-run", "-n", action="store_true", help="Print planned docker run commands without executing")
  return p.parse_args(argv)

def main(argv: List[str]):
  args = _parse_args(argv)

  edition = args.edition
  version = args.version
  test_mode = args.test_mode
  snyk_path = args.snyk_path

  tags = get_all_tags(test_mode, edition, version)

  # Convert to image strings for existing functions that expect strings
  images: List[str] = [tag.as_docker_tag() for tag in tags]

  if not args.dry_run:
    for img in images:
      ensure_image(img)

  planned_commands: List[str] = []
  for i, image in enumerate(images):
    planned_commands.append(f"npx snyk container test {image} --severity-threshold=high --policy-path={snyk_path}")

  if args.dry_run:
    print("Dry run: the following test commands would execute (order preserved):")
    for i, c in enumerate(planned_commands, 1):
      print(f"{i:02d}. {c}")
    return

  for cmd in planned_commands:
    print(cmd)
    subprocess.run(cmd, check=True, shell=True)


if __name__ == "__main__":
  main(sys.argv[1:])