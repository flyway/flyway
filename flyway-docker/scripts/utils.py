import re


def generate_tags(version, tag_suffix):
    tags = ["latest", version, get_major_and_minor_version(version), get_major_version(version)]
    return [t + tag_suffix for t in tags]


def get_major_and_minor_version(version_number):
    return re.match(r"\d+\.\d+", version_number).group(0)


def get_major_version(version_number):
    return re.match(r"\d+", version_number).group(0)