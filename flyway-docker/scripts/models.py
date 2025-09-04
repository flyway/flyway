from dataclasses import dataclass
from typing import Dict, Callable

@dataclass(frozen=True)
class Variant:
    name: str
    suffix: str  # appended to :version
    folder: str  # dockerfiles/<folder>/Dockerfile

@dataclass(frozen=True)
class EditionOverlay:
    """Defines an overlay (e.g., redgate) applied ON TOP of flyway variant images."""
    name: str  # edition name (redgate)
    dockerfiles: Dict[str, str]  # variant.name -> dockerfile path

@dataclass(frozen=True)
class FinalLayer:
    """Defines a final layer (e.g., mongo) applied on top of base or overlay images."""
    name: str  # layer suffix appended (e.g., mongo)
    dockerfile: str
    # function deciding if layer applies to a given image (image_tag_without_layer, edition, variant)
    applies: Callable[[str, str, Variant], bool]
