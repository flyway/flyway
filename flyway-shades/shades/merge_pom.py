import sys
from lxml import etree

def get_key(elem):
    keys = []
    for key in ['groupId', 'artifactId', 'id', 'name']:
        found = elem.find(f".//{{*}}{key}")
        if found is not None:
            keys.append(found.text)
    return f"{elem.tag}:{'|'.join(keys)}" if keys else elem.tag

def merge_children_ordered(repo_elem, partial_elem):
    repo_children = {get_key(child): child for child in repo_elem}
    partial_children = [child for child in partial_elem]
    new_children = []
    for p_child in partial_children:
        key = get_key(p_child)
        if key in repo_children:
            if len(repo_children[key]) > 0 and len(p_child) > 0:
                merge_children_ordered(repo_children[key], p_child)
            else:
                idx = list(repo_elem).index(repo_children[key])
                repo_elem.remove(repo_children[key])
                repo_elem.insert(idx, p_child)
        else:
            new_children.append(p_child)
    for child in new_children:
        repo_elem.append(child)

def merge_plugins_ordered(build_elem):
    plugins_elems = build_elem.findall(".//{{*}}plugins")
    if len(plugins_elems) > 1:
        main_plugins = plugins_elems[0]
        for plugins in plugins_elems[1:]:
            for plugin in plugins:
                key = get_key(plugin)
                existing = None
                for idx, mp in enumerate(main_plugins):
                    if get_key(mp) == key:
                        existing = mp
                        break
                if existing:
                    merge_children_ordered(existing, plugin)
                else:
                    main_plugins.append(plugin)
            parent = plugins.getparent()
            parent.remove(plugins)

def merge_root_ordered(repo_root, partial_root):
    repo_tags = {child.tag: child for child in repo_root}
    partial_tags = [child for child in partial_root]
    for idx, partial_elem in enumerate(partial_tags):
        repo_elem = repo_root.find(partial_elem.tag, namespaces=repo_root.nsmap)
        if repo_elem is not None and len(partial_elem) > 0:
            if partial_elem.tag.endswith('dependencies') or partial_elem.tag.endswith('plugins'):
                repo_items = {get_key(child): child for child in repo_elem}
                partial_items = [child for child in partial_elem]
                for p_item in partial_items:
                    key = get_key(p_item)
                    if key in repo_items:
                        merge_children_ordered(repo_items[key], p_item)
                    else:
                        repo_elem.append(p_item)
            elif partial_elem.tag.endswith('build'):
                for p_child in partial_elem:
                    r_child = repo_elem.find(p_child.tag, namespaces=repo_elem.nsmap)
                    if r_child is not None:
                        merge_children_ordered(r_child, p_child)
                    else:
                        repo_elem.append(p_child)
                merge_plugins_ordered(repo_elem)
            else:
                merge_children_ordered(repo_elem, partial_elem)
        else:
            repo_root.insert(idx, partial_elem)

def merge_pom(partial_xml, repo_xml):
    parser = etree.XMLParser(remove_blank_text=True)
    partial_tree = etree.fromstring(partial_xml, parser)
    repo_tree = etree.fromstring(repo_xml, parser)
    merge_root_ordered(repo_tree, partial_tree)
    return etree.tostring(repo_tree, pretty_print=True, xml_declaration=True, encoding="UTF-8")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python merge_pom.py <partial_pom.xml> <repo_pom.xml>")
        sys.exit(1)
    with open(sys.argv[1], "rb") as f:
        partial_xml = f.read()
    with open(sys.argv[2], "rb") as f:
        repo_xml = f.read()
    merged = merge_pom(partial_xml, repo_xml)
    with open(sys.argv[2], "wb") as f:
        f.write(merged)
    print(f"Merged {sys.argv[1]} into {sys.argv[2]}")

