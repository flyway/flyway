import argparse

def bool_arg(value: str) -> bool:
  v = value.lower()
  if v in {"true", "1", "yes", "y"}:
    return True
  if v in {"false", "0", "no", "n"}:
    return False
  raise argparse.ArgumentTypeError(f"Invalid bool: {value}")
