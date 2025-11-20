from dataclasses import dataclass
from typing import Optional, Dict
from models import Variant, FinalLayer
from constants import VARIANTS, REDGATE_OVERLAY, FINAL_LAYERS
import re
import os
import subprocess

PLATFORMS = "linux/arm64/v8,linux/amd64"

@dataclass(frozen=True)
class Tag:
    edition: str
    version: str
    variant: Variant
    qualifier: Optional[str] = None
    registry: Optional[str] = None

    def _get_tag(self, edition, version, suffix) -> str:
        q = self.qualifier or ""
        r = self.registry or ""
        return f"{r}{edition}/flyway:{version}{suffix}{q}"

    def as_docker_tag(self) -> str:
        return self._get_tag(self.edition, self.version, self.variant.suffix)

    def as_latest(self) -> str:
        return self._get_tag(self.edition, "latest", self.variant.suffix)

    def as_major(self) -> str:
        return self._get_tag(self.edition, get_major_version(self.version), self.variant.suffix)

    def as_minor(self) -> str:
        return self._get_tag(self.edition, get_minor_version(self.version), self.variant.suffix)

    def without_registry(self):
        return Tag(self.edition, self.version, self.variant, self.qualifier)

    def with_registry(self, registry: str):
        return Tag(self.edition, self.version, self.variant, self.qualifier, registry)

    def with_edition(self, edition: str):
        return Tag(edition, self.version, self.variant, self.qualifier, self.registry)

def get_minor_version(version_number):
  return re.match(r"\d+\.\d+", version_number).group(0)


def get_major_version(version_number):
  return re.match(r"\d+", version_number).group(0)

def get_tag_flag(tag: Tag) -> str:
    return f'-t {tag.as_docker_tag()} '

def get_build_args(base_tag: Tag, extra_build_args: dict[str, str]) -> str:
    build_args_parts = [f'--build-arg BASE_IMAGE={base_tag.as_docker_tag()}']
    if extra_build_args:
        for k, v in extra_build_args.items():
            if v is not None:
                build_args_parts.append(f'--build-arg {k}={v}')
    build_args_segment = ' '.join(build_args_parts)
    return build_args_segment


def get_layer_image_tag(base_tag: Tag, layer: FinalLayer) -> Tag:
  return Tag(
      edition=base_tag.edition,
      version=base_tag.version,
      variant=base_tag.variant,
      qualifier=f"{base_tag.qualifier if base_tag.qualifier else ""}-{layer.name}",
      registry=base_tag.registry)

def get_buildx_command(
    edition: str,
    version: str,
    variant: Variant,
):
    tag = Tag(edition=edition, version=version, variant=variant, registry="redgate.azurecr.io/")
    command = f"""
docker buildx build \\
  -f ./dockerfiles/{variant.folder}/Dockerfile \\
  --builder multi_arch_builder \\
  --platform {PLATFORMS} \\
  --build-arg FLYWAY_VERSION={version} --build-arg EDITION={edition} \\
  --output type=registry,name={tag.as_docker_tag()} \\
  --output type=docker,name={tag.as_docker_tag()} --load \\
  ."""
    return command, tag

def get_build_command(
    edition: str,
    version: str,
    variant: Variant,
):
    file_flag: str = f'-f ./dockerfiles/{variant.folder}/Dockerfile '
    build_args: str = f'--pull --build-arg FLYWAY_VERSION={version} --build-arg EDITION={edition}'
    tag = Tag(edition=edition, version=version, variant=variant)
    tag_flag = get_tag_flag(tag)
    command = f'docker build {file_flag} {build_args} {tag_flag}.'
    return command, tag

def get_custom_build_command(
    image_tag: Tag,
    base_tag: Tag,
    dockerfile: str,
    extra_build_args: Optional[Dict[str, str]] = None,
) -> str:
    build_args_segment: str = get_build_args(base_tag, extra_build_args)
    command = f'docker build {build_args_segment} -t {image_tag.as_docker_tag()} -f {dockerfile} .'
    return command

def get_custom_buildx_command(
    image_tag: Tag,
    base_tag: Tag,
    dockerfile: str,
    extra_build_args: Optional[Dict[str, str]] = None,
) -> str:
  command = f"""
docker buildx build \\
  -f {dockerfile} \\
  --builder multi_arch_builder \\
  --platform {PLATFORMS} \\
  {get_build_args(base_tag, extra_build_args)} \\
  --output type=registry,name={image_tag.as_docker_tag()} \\
  --output type=docker,name={image_tag.as_docker_tag()} --load \\
  ."""
  return command

def layer_command(base_tag: Tag, layer: FinalLayer, use_buildx: bool) -> tuple[str, Tag]:  # Updated type hint to use FinalLayer
  fn = get_custom_buildx_command if use_buildx else get_custom_build_command
  image_tag = get_layer_image_tag(base_tag, layer)
  return fn(
        image_tag=image_tag,
        base_tag=base_tag,
        dockerfile=layer.dockerfile,
  ), image_tag

def ensure_image(tag: str) -> None:
  """Fail fast if an expected image tag is not present locally."""
  inspect_cmd = f'docker image inspect {tag} >$null 2>&1' if os.name == 'nt' else f'docker image inspect {tag} >/dev/null 2>&1'
  result = subprocess.run(inspect_cmd, shell=True)
  if result.returncode != 0:
    raise SystemExit(f"Required image '{tag}' not found locally. Run build_images.py first for edition/version (got edition tag missing).")

def get_all_tags(test_mode, edition, version):
  use_buildx = (edition != REDGATE_OVERLAY.name) and (not test_mode)
  variants = [v for v in VARIANTS if (not test_mode) or v.name == "base"]
  if use_buildx:
    tags = [Tag(edition=edition, version=version, variant=v, registry="redgate.azurecr.io/") for v in variants]
  else:
    tags = [Tag(edition=edition, version=version, variant=v) for v in variants]

  # Add mongo variant (using base variant with mongo qualifier)
  base_variant = next(v for v in VARIANTS if v.name == "base")
  base_tag = next(t for t in tags if t.variant.name == "base")
  final_layer_tags = [get_layer_image_tag(base_tag, layer) for layer in FINAL_LAYERS if layer.applies(base_tag.as_docker_tag(), edition, base_variant)]
  tags += final_layer_tags
  return tags
