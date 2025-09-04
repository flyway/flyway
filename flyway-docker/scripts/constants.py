"""
Centralized constants for Docker build configuration.
Add new variants, overlays, or layers here.
"""
from typing import List
from models import Variant, EditionOverlay, FinalLayer

# ---------------------------------------------------------------------------
# Declarative configuration (add new variants or layers here)
# ---------------------------------------------------------------------------

VARIANTS: List[Variant] = [
    Variant(name="base", suffix="", folder="base"),
    Variant(name="alpine", suffix="-alpine", folder="alpine"),
    Variant(name="azure", suffix="-azure", folder="azure"),
]

REDGATE_OVERLAY = EditionOverlay(
    name="redgate",
    dockerfiles={
        "base": "./dockerfiles/base/Redgate.Dockerfile",
        "alpine": "./dockerfiles/alpine/Redgate.Dockerfile",
        "azure": "./dockerfiles/azure/Redgate.Dockerfile",
    },
)

FINAL_LAYERS: List[FinalLayer] = [
    FinalLayer(
        name="mongo",
        dockerfile="./dockerfiles/mongo/Dockerfile",
        applies=lambda image_tag, edition, variant: True,  # applies to all built images
    ),
]
